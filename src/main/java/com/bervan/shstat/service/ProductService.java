package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.logging.BaseProcessContext;
import com.bervan.logging.JsonLogger;
import com.bervan.shstat.AttrFieldMappingVal;
import com.bervan.shstat.AttrMapper;
import com.bervan.shstat.MapperException;
import com.bervan.shstat.entity.*;
import com.bervan.shstat.repository.ProductBestOfferRepository;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    public static final List<AttrFieldMappingVal<Field>> commonProductProperties;
    public static final List<AttrFieldMappingVal<Field>> productPerDateAttributeProperties;

    static {
        try {
            commonProductProperties = List.of(
                    AttrFieldMappingVal.of("Name", Product.class.getDeclaredField("name")),
                    AttrFieldMappingVal.of("Product List Url", Product.class.getDeclaredField("productListUrl")),
                    AttrFieldMappingVal.of("Offer Url", Product.class.getDeclaredField("offerUrl")),
                    AttrFieldMappingVal.of("Shop", Product.class.getDeclaredField("shop")),
                    AttrFieldMappingVal.of("Image", Product.class.getDeclaredField("imgSrc")),
                    AttrFieldMappingVal.of("Categories", Product.class.getDeclaredField("categories"),
                            (val) -> {
                                if (val instanceof Collection<?>) {
                                    return new HashSet<>((Collection<?>) val);
                                }

                                return AttrMapper.mappingError("Categories");
                            }),
                    AttrFieldMappingVal.of("Product List Name", Product.class.getDeclaredField("productListName"))
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            productPerDateAttributeProperties = List.of(
                    AttrFieldMappingVal.of("Price", ProductBasedOnDateAttributes.class.getDeclaredField("price"),
                            (val) -> {
                                if (val == null || val.toString().isBlank()) {
                                    return BigDecimal.valueOf(-1);
                                }
                                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(val.toString()));
                                if (price.compareTo(BigDecimal.valueOf(900000)) > 0) {
                                    return AttrMapper.mappingError("Price");
                                }
                                return price;
                            }),
                    AttrFieldMappingVal.of("Date", ProductBasedOnDateAttributes.class.getDeclaredField("scrapDate"),
                            (val) -> {
                                if (val instanceof Long) {
                                    return new Date((Long) val);
                                } else if (val instanceof Date) {
                                    return val;
                                }

                                return AttrMapper.mappingError("Date");
                            }),
                    AttrFieldMappingVal.of("Formatted Date", ProductBasedOnDateAttributes.class.getDeclaredField("formattedScrapDate"))
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final JsonLogger log = JsonLogger.getLogger(getClass());
    private final ProductRepository productRepository;
    private final ActualProductService actualProductService;
    private final ProductStatsService productStatsService;
    private final ProductSimilarOffersService productSimilarOffersService;
    private final UserRepository userRepository;
    private final ProductBasedOnDateAttributesService productBasedOnDateAttributesService;
    private final ScrapAuditService scrapAuditService;
    private final ProductBestOfferRepository productBestOfferRepository;
    private User commonUser;

    public ProductService(ProductRepository productRepository,
                          ActualProductService actualProductService,
                          ProductStatsService productStatsService, ProductSimilarOffersService productSimilarOffersService,
                          UserRepository userRepository,
                          ProductBasedOnDateAttributesService productBasedOnDateAttributesService,
                          ScrapAuditService scrapAuditService,
                          ProductBestOfferRepository productBestOfferRepository) {
        this.productRepository = productRepository;
        this.actualProductService = actualProductService;
        this.productStatsService = productStatsService;
        this.productSimilarOffersService = productSimilarOffersService;
        this.userRepository = userRepository;
        this.productBasedOnDateAttributesService = productBasedOnDateAttributesService;
        this.scrapAuditService = scrapAuditService;
        this.productBestOfferRepository = productBestOfferRepository;
    }

    private static <T> Optional<T> findProductAttr(Product product, String key, Class<T> productAttrClass) {
        return (Optional<T>) product.getAttributes().stream().filter(e -> e.getName().equals(key))
                .filter(e -> e.getClass().isAssignableFrom(productAttrClass)).findFirst();
    }

    private static BigDecimal getDiscount(BigDecimal avg, BigDecimal price) {
        if (avg == null || avg.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal discount = BigDecimal.ONE
                    .subtract(price.divide(avg, 10, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            return discount;
        }
    }

    @Async("productTaskExecutor")
    public CompletableFuture<List<Product>> addProductsAsync(List<Map<String, Object>> products, BaseProcessContext addProductsContext) {
        List<Product> allMapped = new LinkedList<>();
        List<String> messages = new LinkedList<>();
        for (Map<String, Object> productMap : products) {
            try {
                Product product = mapProductCommonAttr(productMap);
                ProductBasedOnDateAttributes perDateAttributes = mapProductPerDateAttributes(productMap, product, addProductsContext);

                boolean productDateAttributeAdded = addProductDateAttribute(product, perDateAttributes, addProductsContext);

                Set<ProductAttribute> resAttributes = new HashSet<>();
                for (Map.Entry<String, Object> attrs : productMap.entrySet()) {
                    String key = attrs.getKey();
                    Object value = attrs.getValue();
                    if (value instanceof Date) {
                        log.warn(addProductsContext.map(), "Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof String) {
                        Optional<ProductListTextAttribute> attrOpt = findProductAttr(product, key, ProductListTextAttribute.class);
                        if (attrOpt.isEmpty()) {
                            resAttributes.add(new ProductListTextAttribute(key,
                                    new HashSet<>(Collections.singletonList(((String) value)))));
                        } else {
                            attrOpt.get().getValue().add((String) value);
                        }
                    } else if (value instanceof LocalDate) {
                        log.warn(addProductsContext.map(), "Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof LocalDateTime) {
                        log.warn(addProductsContext.map(), "Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof Number) {
                        log.warn(addProductsContext.map(), "Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        if (!list.isEmpty() && list.get(0) instanceof String) {
                            Optional<ProductListTextAttribute> attrOpt = findProductAttr(product, key, ProductListTextAttribute.class);
                            if (attrOpt.isEmpty()) {
                                resAttributes.add(new ProductListTextAttribute(key, new HashSet<>((List<String>) value)));
                            } else {
                                attrOpt.get().getValue().addAll((List<String>) value);
                            }
                        } else if (!list.isEmpty() && list.get(0) instanceof Number) {
                            log.warn(addProductsContext.map(), "Not implemented for: " + attrs);
                            continue;
                        }
                    } else if (value instanceof String[]) {
                        Optional<ProductListTextAttribute> attrOpt = findProductAttr(product, key, ProductListTextAttribute.class);
                        if (attrOpt.isEmpty()) {
                            resAttributes.add(new ProductListTextAttribute(key, new HashSet<>(List.of((String[]) value))));
                        } else {
                            attrOpt.get().getValue().addAll(List.of((String[]) value));
                        }
                    } else if (value != null) {
                        Optional<ProductListTextAttribute> attrOpt = findProductAttr(product, key, ProductListTextAttribute.class);
                        if (attrOpt.isEmpty()) {
                            resAttributes.add(new ProductListTextAttribute(key,
                                    new HashSet<>(Collections.singletonList(((String) value)))));
                        } else {
                            attrOpt.get().getValue().add((String) value);
                        }
                    }
                }

                for (ProductAttribute resAttribute : resAttributes) {
                    product.addAttribute(resAttribute);
                }

                loadCommonUserIfNotLoaded();
                product.addOwner(commonUser);
                if (product.getName().length() > 300) {
                    log.error(addProductsContext.map(), "Product name is to long: {}", product.getName());
                    continue;
                }
                if (product.getName().length() < 3) {
                    log.error(addProductsContext.map(), "Product name is to short: {}", product.getName());
                    continue;
                }

                product = save(product, addProductsContext);
                createAndUpdateTokens(product, addProductsContext);
                updateActualProducts(perDateAttributes, product, addProductsContext);
                updateProductStats(product, addProductsContext);

                allMapped.add(product);
            } catch (MapperException e) {
                if (e.isSendErrorMessage() && e.getMessage() != null && !e.getMessage().isEmpty()) {
                    messages.add(e.getMessage());
                }
            }
        }

        for (String message : messages) {
            log.error(addProductsContext.map(), message);
        }

        return CompletableFuture.completedFuture(allMapped);
    }

    public void updateScrapAudit(List<Product> allMapped, BaseProcessContext addProductsContext) {
        String delimiter = "___";

        // group result by "shop", "productListName", "productListUrl"
        Map<String, List<Product>> groupedProducts = allMapped.stream()
                .collect(Collectors.groupingBy(p -> String.join(delimiter,
                        Optional.ofNullable(p.getShop()).orElse(""),
                        Optional.ofNullable(p.getProductListName()).orElse(""),
                        Optional.ofNullable(p.getProductListUrl()).orElse(""))));

        groupedProducts.forEach((key, list) -> {
            String[] split = key.split(delimiter);
            scrapAuditService.updateSavedProductsCount(split[0], split[1], split[2], list.size(), addProductsContext);
        });
    }

    private void updateProductStats(Product product, BaseProcessContext addProductsContext) {
        try {
            productStatsService.updateProductStats(product, commonUser, addProductsContext);
        } catch (Exception e) {
            log.error(addProductsContext.map(), "Failed to updateProductStats!", e);
            throw new MapperException("Failed to updateProductStats!");
        }
    }

    private void updateActualProducts(ProductBasedOnDateAttributes perDateAttributes, Product product, BaseProcessContext addProductsContext) {
        try {
            actualProductService.updateActualProducts(perDateAttributes.getScrapDate(), product, commonUser, addProductsContext);
        } catch (Exception e) {
            log.error(addProductsContext.map(), "Failed to updateActualProducts!", e);
            throw new MapperException("Failed to updateActualProducts!");
        }
    }

    private void createAndUpdateTokens(Product product, BaseProcessContext addProductsContext) {
        try {
            productSimilarOffersService.createAndUpdateTokens(product, commonUser, actualProductService);
        } catch (Exception e) {
            log.error(addProductsContext.map(), "Failed to createAndUpdateTokens!", e);
        }
    }

    private synchronized Product save(Product product, BaseProcessContext addProductsContext) {
        try {
            // if in 1 portion of data we will have the same product that has not been added to db, then it will be added more than 1 times,
            // and org.hibernate.exception.ConstraintViolationException will be thrown
            // in standard flow it should not happen...
            if (product.getId() == null) {
                return productRepository.save(product);
            }
            return productRepository.save(product);
        } catch (Exception e) {
            log.error(addProductsContext.map(), "Failed to save/update product!", e);
            throw new MapperException("Failed to save/update product!");
        }
    }

    public void updateStats(Product product) {
        Optional<ProductStats> byProductId = productStatsService.findByProductId(product.getId());
        productStatsService.updateStatsAndSave(byProductId, product);
    }

    private void loadCommonUserIfNotLoaded() {
        if (commonUser == null) {
            commonUser = userRepository.findByUsername("COMMON_USER").get();
        }
    }

    private boolean addProductDateAttribute(Product product, ProductBasedOnDateAttributes newPerDateAttribute, BaseProcessContext addProductsContext) {
        List<ProductBasedOnDateAttributes> sortedPrices = new ArrayList<>(product.getProductBasedOnDateAttributes().stream()
                .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                .toList());
        product.setProductBasedOnDateAttributes(sortedPrices);

        boolean shouldNewProductBaseOnDateAttributeCreated = shouldNewProductBaseOnDateAttributeCreated(product, sortedPrices, newPerDateAttribute, addProductsContext);
        if (shouldNewProductBaseOnDateAttributeCreated) {
            product.addAttribute(newPerDateAttribute);
        }

        ProductBasedOnDateAttributesService.moveScrapDates(product.getProductBasedOnDateAttributes());

        return shouldNewProductBaseOnDateAttributeCreated;
    }

    private boolean shouldNewProductBaseOnDateAttributeCreated(Product product, List<ProductBasedOnDateAttributes> sortedPrices, ProductBasedOnDateAttributes newPerDateAttribute, BaseProcessContext addProductsContext) {
        if (!sortedPrices.isEmpty()) {
            ProductBasedOnDateAttributes lastAttr = sortedPrices.get(0);
            BigDecimal previousPrice = lastAttr.getPrice();
            BigDecimal currentPrice = newPerDateAttribute.getPrice();
            BigDecimal difference = previousPrice.subtract(currentPrice).abs();
            BigDecimal threshold = previousPrice.multiply(BigDecimal.valueOf(0.009)); // 0.9% threshold

            if (difference.compareTo(threshold) < 0) {
                log.warn(addProductsContext.map(), "New ProductBasedOnDateAttribute will not be created because new price is almost the same as previous one (less than 1% change). Product: {}, Old price {}, new price: {}",
                        product.getName(), previousPrice, currentPrice);
                return false;
            }
        }

        if (product.getId() != null && sortedPrices.size() > 10) {
            BigDecimal sum = BigDecimal.valueOf(1);
            for (ProductBasedOnDateAttributes productBasedOnDateAttribute : product.getProductBasedOnDateAttributes()) {
                sum = sum.add(productBasedOnDateAttribute.getPrice());
            }

            if (newPerDateAttribute.getPrice().compareTo(BigDecimal.valueOf(2)
                    .multiply(sum.divide(BigDecimal.valueOf(product.getProductBasedOnDateAttributes().size()),
                            RoundingMode.CEILING))) >= 0
                    && newPerDateAttribute.getPrice().subtract(BigDecimal.valueOf(10000)).compareTo(BigDecimal.ONE) >= 0) {
                //if product has at least 10 prices and new price is much bigger than previous, and newPrice - 10000 >= 1 - we skip adding the price
                log.warn(addProductsContext.map(), "ProductBasedOnDateAttribute skipped because the new price is much bigger than average: {} -> {}",
                        product.getName(), newPerDateAttribute.getPrice());
                return false;
            }
        }


        if (newPerDateAttribute.getPrice().compareTo(BigDecimal.ONE) >= 0) {
            return true;
        }
        return false;
    }

    private ProductBasedOnDateAttributes mapProductPerDateAttributes(Map<String, Object> productToMap, Product product, BaseProcessContext addProductsContext) {
        BeanWrapper wrapper = new BeanWrapperImpl(ProductBasedOnDateAttributes.class);
        Map<String, Object> productProperties = new HashMap<>();
        for (AttrFieldMappingVal<Field> perDateAttrs : productPerDateAttributeProperties) {
            try {
                Object value = productToMap.get(perDateAttrs.attr);
                Field field = perDateAttrs.val;
                value = perDateAttrs.mapper.map(value);
                productProperties.put(field.getName(), value);
                productToMap.remove(perDateAttrs.attr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        wrapper.setPropertyValues(productProperties);
        ProductBasedOnDateAttributes res = (ProductBasedOnDateAttributes) wrapper.getWrappedInstance();

        if (product.getId() != null &&
                productBasedOnDateAttributesService.existsByProductIdAndFormattedScrapDate(product.getId(), res.getFormattedScrapDate())) {
            log.warn(addProductsContext.map(), "Product {} ({}) was already mapped for given date!\nShop:{}\nProductListName:{}\nScrapDate:{}",
                    product.getName(),
                    product.getId(),
                    product.getShop(), product.getProductListName(),
                    res.getFormattedScrapDate());
            throw new MapperException(new StringFormattedMessage("Product %s was already mapped for given date!", product.getName()), false);
        }

        return res;
    }

    private Product findProductBasedOnAttributes(Product res) {
        Optional<Product> product = productRepository.findByNameAndShopAndProductListNameAndProductListUrlAndOfferUrl(res.getName(), res.getShop(),
                res.getProductListName(), res.getProductListUrl()
                , res.getOfferUrl());

        return product.orElse(res);
    }

    private Product mapProductCommonAttr(Map<String, Object> product) {
        BeanWrapper wrapper = new BeanWrapperImpl(Product.class);
        Map<String, Object> productProperties = new HashMap<>();
        for (AttrFieldMappingVal<Field> commonProductProperty : commonProductProperties) {
            try {
                Object value = product.get(commonProductProperty.attr);
                Field field = commonProductProperty.val;
                value = commonProductProperty.mapper.map(value);
                productProperties.put(field.getName(), value);
                product.remove(commonProductProperty.attr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        wrapper.setPropertyValues(productProperties);
        Product res = (Product) wrapper.getWrappedInstance();

        Product productBasedOnAttributes = findProductBasedOnAttributes(res);
        if (productBasedOnAttributes.getId() != null) {
            //update categories
            productBasedOnAttributes.setCategories(res.getCategories());
            if (res.getImgSrc() != null && res.getImgSrc().length() > 10) {
                //update image src
                productBasedOnAttributes.setImgSrc(res.getImgSrc());
            }
        }

        return productBasedOnAttributes;
    }

    @Transactional
    public void createBestOffers() {
        log.info("createBestOffers started");
        productBestOfferRepository.deleteAllOwners();
        productBestOfferRepository.deleteAllItems();
        int pageSize = 1000;
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("id"));
        Page<ActualProduct> page;

        loadCommonUserIfNotLoaded();

        do {
            page = actualProductService.findAll(pageable);
            List<ActualProduct> actualProducts = page.getContent();
            Set<Long> productIds = actualProducts.stream()
                    .map(ActualProduct::getProductId)
                    .collect(Collectors.toSet());

            List<ProductStats> stats = productStatsService.findAllByProductId(productIds);

            Map<Long, ActualProduct> actualProductMap = actualProducts.stream()
                    .collect(Collectors.toMap(
                            ActualProduct::getProductId,
                            Function.identity()
                    ));

            Map<Long, ProductStats> productStatsMap = stats.stream()
                    .collect(Collectors.toMap(
                            ProductStats::getProductId,
                            Function.identity()
                    ));

            List<ProductBestOffer> toBeSaved = new ArrayList<>();

            for (Long productId : productIds) {
                ActualProduct actualProduct = actualProductMap.get(productId);
                ProductStats productStats = productStatsMap.get(productId);

                if (productStats == null) continue;

                ProductBestOffer productBestOffer = new ProductBestOffer();
                productBestOffer.setProductId(actualProduct.getProductId());
                productBestOffer.setProductName(actualProduct.getProductName());
                productBestOffer.setPrice(actualProduct.getPrice());
                productBestOffer.setShop(actualProduct.getShop());

                BigDecimal price = actualProduct.getPrice();
                boolean atLeastOneDiscount = false;

                atLeastOneDiscount |= calculateDiscount(productStats.getAvg1Month(), price, productBestOffer::setDiscount1Month);
                atLeastOneDiscount |= calculateDiscount(productStats.getAvg2Month(), price, productBestOffer::setDiscount2Month);
                atLeastOneDiscount |= calculateDiscount(productStats.getAvg3Month(), price, productBestOffer::setDiscount3Month);
                atLeastOneDiscount |= calculateDiscount(productStats.getAvg6Month(), price, productBestOffer::setDiscount6Month);
                atLeastOneDiscount |= calculateDiscount(productStats.getAvg12Month(), price, productBestOffer::setDiscount12Month);

                if (atLeastOneDiscount) {
                    productBestOffer.addOwner(commonUser);
                    toBeSaved.add(productBestOffer);
                }
            }

            productBestOfferRepository.saveAll(toBeSaved);
            pageable = page.nextPageable();
        } while (page.hasNext());
        log.info("createBestOffers ended");
    }

    private boolean calculateDiscount(BigDecimal avg, BigDecimal price, Consumer<BigDecimal> setter) {
        if (avg == null || price == null) {
            setter.accept(BigDecimal.ZERO);
            return false;
        }
        BigDecimal discount = getDiscount(avg, price);
        if (discount.doubleValue() > 0) {
            setter.accept(discount);
            return true;
        } else {
            setter.accept(BigDecimal.ZERO);
            return false;
        }
    }

    public Product update(Long id, String name, String link, String finalImage) {
        Product product = productRepository.findById(id).get();
        product.setName(name);
        product.setOfferUrl(link);
        product.setImgSrc(finalImage);

        return productRepository.save(product);
    }
}
