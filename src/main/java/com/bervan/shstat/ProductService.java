package com.bervan.shstat;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.entity.ProductListTextAttribute;
import com.bervan.shstat.repository.ProductBasedOnDateAttributesRepository;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.response.AddProductApiResponse;
import com.bervan.shstat.response.ApiResponse;
import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ActualProductService actualProductService;
    private final ProductStatsService productStatsService;
    private final UserRepository userRepository;
    private final ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository;
    public static final List<AttrFieldMappingVal<Field>> commonProductProperties;
    public static final List<AttrFieldMappingVal<Field>> productPerDateAttributeProperties;
    @PersistenceContext
    private EntityManager entityManager;

    static {
        try {
            commonProductProperties = List.of(
                    AttrFieldMappingVal.of("Name", Product.class.getDeclaredField("name")),
                    AttrFieldMappingVal.of("Product List Url", Product.class.getDeclaredField("productListUrl")),
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
                                if (val.toString().isBlank()) {
                                    return BigDecimal.valueOf(-1);
                                }
                                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(val.toString()));
                                if (price.compareTo(BigDecimal.valueOf(90000)) > 0) {
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
                          ProductStatsService productStatsService, UserRepository userRepository, ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository) {
        this.productRepository = productRepository;
        this.actualProductService = actualProductService;
        this.productStatsService = productStatsService;
        this.userRepository = userRepository;
        this.productBasedOnDateAttributesRepository = productBasedOnDateAttributesRepository;
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
        for (Map<String, Object> product : products) {
            try {
                Object date = product.get("Date");
                Object price = product.get("Price");
                Product mappedProduct = mapProduct(product);
                User commonUser = userRepository.findByUsername("COMMON_USER").get();
                mappedProduct.addOwner(commonUser);
                mappedProduct = productRepository.save(mappedProduct);
                actualProductService.updateActualProducts(date, mappedProduct, commonUser);
                productStatsService.updateProductStats(mappedProduct, price, commonUser);
                allMapped.add(mappedProduct);
            } catch (MapperException e) {
                if (e.isSendErrorMessage() && e.getMessage() != null && !e.getMessage().isEmpty()) {
                    messages.add(e.getMessage());
                }
            }
        }

        return new AddProductApiResponse(messages, allMapped.size(), products.size());
    }


    private Product mapProduct(Map<String, Object> productToMap) {
        Product product = mapProductCommonAttr(productToMap);
        ProductBasedOnDateAttributes perDateAttributes = mapProductPerDateAttributes(productToMap, product);
        product.addAttribute(perDateAttributes);
        Set<ProductAttribute> resAttributes = new HashSet<>();
        for (Map.Entry<String, Object> attrs : productToMap.entrySet()) {
            String key = attrs.getKey();
            Object value = attrs.getValue();
            if (value instanceof Date) {
                throw new RuntimeException("Not implemented for: " + attrs);
            } else if (value instanceof String) {
                Optional<ProductListTextAttribute> attrOpt = findProductAttr(product, key, ProductListTextAttribute.class);
                if (attrOpt.isEmpty()) {
                    resAttributes.add(new ProductListTextAttribute(key,
                            new HashSet<>(Collections.singletonList(((String) value)))));
                } else {
                    attrOpt.get().getValue().add((String) value);
                }
            } else if (value instanceof LocalDate) {
                throw new RuntimeException("Not implemented for: " + attrs);
            } else if (value instanceof LocalDateTime) {
                throw new RuntimeException("Not implemented for: " + attrs);
            } else if (value instanceof Number) {
                throw new RuntimeException("Not implemented for: " + attrs);
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
                    throw new RuntimeException("Not implemented for: " + attrs);
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

        return product;
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
                productBasedOnDateAttributesRepository.existsByProductAndScrapDate(product, res.getScrapDate())) {
            throw new MapperException(new StringFormattedMessage("Product %s was already mapped for given date!", product.getName()), false);
        }

        return res;
    }

    private Product findProductBasedOnAttributes(Product res) {
//        System.out.println("Looking for: " + res.getName() + ":");
        Optional<Product> product = productRepository.findByNameAndShopAndProductListNameAndProductListUrl(res.getName(), res.getShop(),
                res.getProductListName(), res.getProductListUrl());

        if (product.isPresent()) {
            //update categories
            product.get().setCategories(res.getCategories());
            if (res.getImgSrc() != null && res.getImgSrc().length() > 10) {
                //update image src
                product.get().setImgSrc(res.getImgSrc());
            }
            return product.get();
        }
        return res;
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

        return findProductBasedOnAttributes(res);
    }

    @Transactional
    public void lowerThanAVGForLastXMonths() {
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
    }

    private static String getSql(int months) {
        return " WITH RankedPrices AS (SELECT DISTINCT product_id, avg" + months + "month AS average_price FROM product_stats) " +
                " SELECT DISTINCT pda.id AS id, pda.scrap_date AS scrap_date, pda.price AS price, rp.average_price AS avgPrice, " + months + " AS month_offset, " +
                """
                                p.name AS product_name, p.shop as shop, pc.categories AS category, p.img_src AS product_image_src,
                                (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) AS discount_in_percent
                        FROM product_based_on_date_attributes pda
                                JOIN product p ON p.id = pda.product_id
                                JOIN RankedPrices rp ON p.id = rp.product_id
                                LEFT JOIN product_categories pc ON pda.product_id = pc.product_id
                                JOIN actual_product ap ON ap.product_id = pda.product_id AND ap.scrap_date = pda.scrap_date
                        WHERE pda.price < rp.average_price AND pda.price > 0
                                ORDER BY pda.id;
                        """;
    }
}
