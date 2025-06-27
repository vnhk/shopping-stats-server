package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.AttrFieldMappingVal;
import com.bervan.shstat.AttrMapper;
import com.bervan.shstat.MapperException;
import com.bervan.shstat.entity.*;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    private final ProductRepository productRepository;
    private final ActualProductService actualProductService;
    private final ProductStatsService productStatsService;
    private final ProductSimilarOffersService productSimilarOffersService;
    private final UserRepository userRepository;
    private final ProductBasedOnDateAttributesService productBasedOnDateAttributesService;
    private final ScrapAuditService scrapAuditService;
    @PersistenceContext
    private EntityManager entityManager;
    private User commonUser;

    public ProductService(ProductRepository productRepository,
                          ActualProductService actualProductService,
                          ProductStatsService productStatsService, ProductSimilarOffersService productSimilarOffersService,
                          UserRepository userRepository,
                          ProductBasedOnDateAttributesService productBasedOnDateAttributesService,
                          ScrapAuditService scrapAuditService) {
        this.productRepository = productRepository;
        this.actualProductService = actualProductService;
        this.productStatsService = productStatsService;
        this.productSimilarOffersService = productSimilarOffersService;
        this.userRepository = userRepository;
        this.productBasedOnDateAttributesService = productBasedOnDateAttributesService;
        this.scrapAuditService = scrapAuditService;
    }

    private static <T> Optional<T> findProductAttr(Product product, String key, Class<T> productAttrClass) {
        return (Optional<T>) product.getAttributes().stream().filter(e -> e.getName().equals(key))
                .filter(e -> e.getClass().isAssignableFrom(productAttrClass)).findFirst();
    }

    @Async("productTaskExecutor")
    public CompletableFuture<List<Product>> addProductsAsync(List<Map<String, Object>> products) {
        List<Product> allMapped = new LinkedList<>();
        List<String> messages = new LinkedList<>();
        for (Map<String, Object> productMap : products) {
            try {
                Product product = mapProductCommonAttr(productMap);
                ProductBasedOnDateAttributes perDateAttributes = mapProductPerDateAttributes(productMap, product);

                boolean productDateAttributeAdded = addProductDateAttribute(product, perDateAttributes);

                Set<ProductAttribute> resAttributes = new HashSet<>();
                for (Map.Entry<String, Object> attrs : productMap.entrySet()) {
                    String key = attrs.getKey();
                    Object value = attrs.getValue();
                    if (value instanceof Date) {
                        log.warn("Not implemented for: " + attrs);
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
                        log.warn("Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof LocalDateTime) {
                        log.warn("Not implemented for: " + attrs);
                        continue;
                    } else if (value instanceof Number) {
                        log.warn("Not implemented for: " + attrs);
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
                            log.warn("Not implemented for: " + attrs);
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
                    log.error("Product name is to long: {}", product.getName());
                    continue;
                }
                if (product.getName().length() < 3) {
                    log.error("Product name is to short: {}", product.getName());
                    continue;
                }

                product = save(product);
                createAndUpdateTokens(product);
                updateActualProducts(perDateAttributes, product);
                updateProductStats(product);

                allMapped.add(product);
            } catch (MapperException e) {
                if (e.isSendErrorMessage() && e.getMessage() != null && !e.getMessage().isEmpty()) {
                    messages.add(e.getMessage());
                }
            }
        }

        for (String message : messages) {
            log.error(message);
        }

        return CompletableFuture.completedFuture(allMapped);
    }

    public void updateScrapAudit(List<Product> allMapped) {
        String delimiter = "___";

        // group result by "shop", "productListName", "productListUrl"
        Map<String, List<Product>> groupedProducts = allMapped.stream()
                .collect(Collectors.groupingBy(p -> String.join(delimiter,
                        Optional.ofNullable(p.getShop()).orElse(""),
                        Optional.ofNullable(p.getProductListName()).orElse(""),
                        Optional.ofNullable(p.getProductListUrl()).orElse(""))));

        groupedProducts.forEach((key, list) -> {
            String[] split = key.split(delimiter);
            scrapAuditService.updateSavedProductsCount(split[0], split[1], split[2], list.size());
        });
    }

    private void updateProductStats(Product product) {
        try {
            productStatsService.updateProductStats(product, commonUser);
        } catch (Exception e) {
            log.error("Failed to updateProductStats!", e);
            throw new MapperException("Failed to updateProductStats!");
        }
    }

    private void updateActualProducts(ProductBasedOnDateAttributes perDateAttributes, Product product) {
        try {
            actualProductService.updateActualProducts(perDateAttributes.getScrapDate(), product, commonUser);
        } catch (Exception e) {
            log.error("Failed to updateActualProducts!", e);
            throw new MapperException("Failed to updateActualProducts!");
        }
    }

    private void createAndUpdateTokens(Product product) {
        try {
            productSimilarOffersService.createAndUpdateTokens(product, commonUser);
        } catch (Exception e) {
            log.error("Failed to createAndUpdateTokens!", e);
        }
    }


    private synchronized Product save(Product product) {
        try {
            // if in 1 portion of data we will have the same product that has not been added to db, then it will be added more than 1 times,
            // and org.hibernate.exception.ConstraintViolationException will be thrown
            // in standard flow it should not happen...
            if (product.getId() == null) {
                return productRepository.save(product);
            }
            return productRepository.save(product);
        } catch (Exception e) {
            log.error("Failed to save/update product!", e);
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

    private boolean addProductDateAttribute(Product product, ProductBasedOnDateAttributes newPerDateAttribute) {
        List<ProductBasedOnDateAttributes> sortedPrices = new ArrayList<>(product.getProductBasedOnDateAttributes().stream()
                .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                .toList());
        product.setProductBasedOnDateAttributes(sortedPrices);

        boolean shouldNewProductBaseOnDateAttributeCreated = shouldNewProductBaseOnDateAttributeCreated(product, sortedPrices, newPerDateAttribute);
        if (shouldNewProductBaseOnDateAttributeCreated) {
            product.addAttribute(newPerDateAttribute);
        }

        ProductBasedOnDateAttributesService.moveScrapDates(product.getProductBasedOnDateAttributes());

        return shouldNewProductBaseOnDateAttributeCreated;
    }

    private boolean shouldNewProductBaseOnDateAttributeCreated(Product product, List<ProductBasedOnDateAttributes> sortedPrices, ProductBasedOnDateAttributes newPerDateAttribute) {
        if (!sortedPrices.isEmpty()) {
            ProductBasedOnDateAttributes lastAttr = sortedPrices.get(0);
            BigDecimal previousPrice = lastAttr.getPrice();
            BigDecimal currentPrice = newPerDateAttribute.getPrice();
            BigDecimal difference = previousPrice.subtract(currentPrice).abs();
            BigDecimal threshold = previousPrice.multiply(BigDecimal.valueOf(0.009)); // 0.9% threshold

            if (difference.compareTo(threshold) < 0) {
                log.warn("New ProductBasedOnDateAttribute will not be created because new price is almost the same as previous one (less than 1% change). Product: {}, Old price {}, new price: {}",
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
                log.warn("ProductBasedOnDateAttribute skipped because the new price is much bigger than average: {} -> {}",
                        product.getName(), newPerDateAttribute.getPrice());
                return false;
            }
        }


        if (newPerDateAttribute.getPrice().compareTo(BigDecimal.ONE) >= 0) {
            return true;
        }
        return false;
    }

    private ProductBasedOnDateAttributes mapProductPerDateAttributes(Map<String, Object> productToMap, Product product) {
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
            log.warn("Product {} ({}) was already mapped for given date!\nShop:{}\nProductListName:{}\nScrapDate:{}",
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
    public void createLowerThanAVGForLastXMonths() {
        List<Integer> monthOffsets = List.of(1, 2, 3, 6, 12);

        // CREATE OR REPLACE TABLE with first offset
        String createTableQuery = "CREATE OR REPLACE TABLE LOWER_THAN_AVG_FOR_X_MONTHS AS ";
        entityManager.createNativeQuery(createTableQuery + getSql(monthOffsets.get(0))).executeUpdate();

        // INSERT INTO with remaining offsets
        String insertIntoQuery = "INSERT INTO LOWER_THAN_AVG_FOR_X_MONTHS ";
        for (int i = 1; i < monthOffsets.size(); i++) {
            entityManager.createNativeQuery(insertIntoQuery + getSql(monthOffsets.get(i))).executeUpdate();
        }

        // Create indexes (re-created each time table is replaced)
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_main_filter_full ON LOWER_THAN_AVG_FOR_X_MONTHS(month_offset, avgPrice, discount_in_percent, category, shop)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_main_filter_partial ON LOWER_THAN_AVG_FOR_X_MONTHS(month_offset, avgPrice, discount_in_percent)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_id ON LOWER_THAN_AVG_FOR_X_MONTHS(id)").executeUpdate();
    }

    private String getSql(int months) {
        return "WITH RankedPrices AS (" +
                "    SELECT DISTINCT product_id, avg" + months + "month AS average_price FROM product_stats" +
                ") " +
                "SELECT DISTINCT pda.id AS id, pda.scrap_date AS scrap_date, pda.price AS price, " +
                "       rp.average_price AS avgPrice, " + months + " AS month_offset, " +
                "       UPPER(p.name) AS product_name, p.shop AS shop, pc.categories AS category, " +
                "       p.img_src AS product_image_src, " +
                "       (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) AS discount_in_percent " +
                "FROM product_based_on_date_attributes pda " +
                "JOIN product p ON p.id = pda.product_id " +
                "JOIN RankedPrices rp ON p.id = rp.product_id " +
                "LEFT JOIN product_categories pc ON pda.product_id = pc.product_id " +
                "JOIN actual_product ap ON ap.product_id = pda.product_id " +
                "WHERE pda.price < rp.average_price " +
                "  AND pda.price > 0 " +
                "  AND ((1 - pda.price / rp.average_price) * 100) >= 5 " +
                "ORDER BY pda.id";
    }

    public void update(Long id, String name, String link, String finalImage) {
        Product product = productRepository.findById(id).get();
        product.setName(name);
        product.setOfferUrl(link);
        product.setImgSrc(finalImage);

        productRepository.save(product);
    }
}
