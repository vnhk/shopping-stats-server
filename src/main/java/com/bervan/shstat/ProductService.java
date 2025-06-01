package com.bervan.shstat;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.entity.*;
import com.bervan.shstat.repository.ProductBasedOnDateAttributesRepository;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.response.AddProductApiResponse;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ActualProductService actualProductService;
    private final ProductStatsService productStatsService;
    private final ProductSimilarOffersService productSimilarOffersService;
    private final UserRepository userRepository;
    private final ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository;
    public static final List<AttrFieldMappingVal<Field>> commonProductProperties;
    public static final List<AttrFieldMappingVal<Field>> productPerDateAttributeProperties;
    @PersistenceContext
    private EntityManager entityManager;
    private User commonUser;
    private final ScrapAuditService scrapAuditService;

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

    public ProductService(ProductRepository productRepository,
                          ActualProductService actualProductService,
                          ProductStatsService productStatsService, ProductSimilarOffersService productSimilarOffersService,
                          UserRepository userRepository,
                          ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository,
                          ScrapAuditService scrapAuditService) {
        this.productRepository = productRepository;
        this.actualProductService = actualProductService;
        this.productStatsService = productStatsService;
        this.productSimilarOffersService = productSimilarOffersService;
        this.userRepository = userRepository;
        this.productBasedOnDateAttributesRepository = productBasedOnDateAttributesRepository;
        this.scrapAuditService = scrapAuditService;
    }

    public void addProductsByPartitions(List<Map<String, Object>> products) {
        List<List<Map<String, Object>>> partition = Lists.partition(products, 50);
        for (List<Map<String, Object>> p : partition) {
            ApiResponse apiResponse = addProducts(p);
            if (apiResponse.getMessages() != null && !apiResponse.getMessages().isEmpty()) {
                System.out.println(apiResponse.getMessages());
            }
        }
    }

    @Transactional
    public ApiResponse addProducts(List<Map<String, Object>> products) {
        List<Product> allMapped = new LinkedList<>();
        List<String> messages = new LinkedList<>();
        log.info("Processing started for: {} products", products.size());
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

                product = productRepository.save(product);
                productSimilarOffersService.createAndUpdateTokens(product);
                actualProductService.updateActualProducts(perDateAttributes.getScrapDate(), product, commonUser);

                if (productDateAttributeAdded) {
                    productStatsService.updateProductStats(product, perDateAttributes.getPrice(), commonUser);
                }

                allMapped.add(product);
            } catch (MapperException e) {
                if (e.isSendErrorMessage() && e.getMessage() != null && !e.getMessage().isEmpty()) {
                    messages.add(e.getMessage());
                }
            }
        }
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

        log.info("Processing ended for: {} products", allMapped.size());

        return new AddProductApiResponse(messages, allMapped.size(), products.size());
    }

    public void updateStats(Product product) {
        Optional<ProductStats> byProductId = productStatsService.findByProductId(product.getId());
        productStatsService.updateStatsAndSave(byProductId, product.getId());
    }

    private void loadCommonUserIfNotLoaded() {
        if (commonUser == null) {
            commonUser = userRepository.findByUsername("COMMON_USER").get();
        }
    }

    private boolean addProductDateAttribute(Product product, ProductBasedOnDateAttributes newPerDateAttribute) {
        List<ProductBasedOnDateAttributes> sortedPrices = new ArrayList<>();
        if (product.getProductBasedOnDateAttributes() != null) {
            sortedPrices = product.getProductBasedOnDateAttributes().stream()
                    .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                    .toList();
        }

        if (!sortedPrices.isEmpty()) {
            ProductBasedOnDateAttributes lastAttr = sortedPrices.get(0);
            BigDecimal previousPrice = lastAttr.getPrice();
            BigDecimal currentPrice = newPerDateAttribute.getPrice();
            BigDecimal difference = previousPrice.subtract(currentPrice).abs();
            BigDecimal threshold = previousPrice.multiply(BigDecimal.valueOf(0.05)); // 5% threshold

            if (difference.compareTo(threshold) < 0) {
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
                    && newPerDateAttribute.getPrice().subtract(BigDecimal.valueOf(2000)).compareTo(BigDecimal.ONE) >= 0) {
                //if product has at least 10 prices and new price is much bigger than previous, and newPrice - 2000 >= 1 - we skip adding the price
                log.warn("ProductBasedOnDateAttribute skipped because the new price is much bigger than average: {} -> {}",
                        product.getName(), newPerDateAttribute.getPrice());
                return false;
            }
        }


        if (newPerDateAttribute.getPrice().compareTo(BigDecimal.ONE) >= 0) {
            product.addAttribute(newPerDateAttribute);
            return true;
        }
        return false;
    }


    private static <T> Optional<T> findProductAttr(Product product, String key, Class<T> productAttrClass) {
        return (Optional<T>) product.getAttributes().stream().filter(e -> e.getName().equals(key))
                .filter(e -> e.getClass().isAssignableFrom(productAttrClass)).findFirst();
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
                productBasedOnDateAttributesRepository.existsByProductIdAndFormattedScrapDate(product.getId(), res.getFormattedScrapDate())) {
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
        if(productBasedOnAttributes.getId() != null) {
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
        String createTableQuery = "CREATE OR REPLACE TABLE LOWER_THAN_AVG_FOR_X_MONTHS AS";
        String sqlFor1MonthOffset = getSql(1);
        String sqlFor2MonthOffset = getSql(2);
        String sqlFor3MonthOffset = getSql(3);
        String sqlFor6MonthOffset = getSql(6);
        String sqlFor12MonthOffset = getSql(12);
        entityManager.createNativeQuery(createTableQuery + sqlFor1MonthOffset).executeUpdate();
        String insertIntoQuery = "INSERT INTO LOWER_THAN_AVG_FOR_X_MONTHS";
        entityManager.createNativeQuery(insertIntoQuery + sqlFor2MonthOffset).executeUpdate();
        entityManager.createNativeQuery(insertIntoQuery + sqlFor3MonthOffset).executeUpdate();
        entityManager.createNativeQuery(insertIntoQuery + sqlFor6MonthOffset).executeUpdate();
        entityManager.createNativeQuery(insertIntoQuery + sqlFor12MonthOffset).executeUpdate();

        // Create indexes for performance optimization
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_main_filter_full ON LOWER_THAN_AVG_FOR_X_MONTHS(month_offset, avgPrice, discount_in_percent, category, shop)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_main_filter_partial ON LOWER_THAN_AVG_FOR_X_MONTHS(month_offset, avgPrice, discount_in_percent)").executeUpdate();
        entityManager.createNativeQuery("CREATE INDEX idx_ltafxm_id ON LOWER_THAN_AVG_FOR_X_MONTHS(id)").executeUpdate();
    }

    private String getSql(int months) {
        return " WITH RankedPrices AS (SELECT DISTINCT product_id, avg" + months + "month AS average_price FROM product_stats) " +
                " SELECT DISTINCT pda.id AS id, pda.scrap_date AS scrap_date, pda.price AS price, rp.average_price AS avgPrice, " + months + " AS month_offset, " +
                """
                            UPPER(p.name) AS product_name, p.shop as shop, pc.categories AS category, p.img_src AS product_image_src,
                            (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) AS discount_in_percent
                        FROM product_based_on_date_attributes pda
                            JOIN product p ON p.id = pda.product_id
                            JOIN RankedPrices rp ON p.id = rp.product_id
                            LEFT JOIN product_categories pc ON pda.product_id = pc.product_id
                            JOIN actual_product ap ON ap.product_id = pda.product_id AND ap.scrap_date = pda.scrap_date
                        WHERE pda.price < rp.average_price
                            AND pda.price > 0
                            AND ((1 - pda.price / rp.average_price) * 100) >= 5
                        ORDER BY pda.id;
                        """;
    }
}
