package com.shstat;

import com.shstat.entity.Product;
import com.shstat.entity.ProductAttribute;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.entity.ProductListTextAttribute;
import com.shstat.repository.ProductBasedOnDateAttributesRepository;
import com.shstat.repository.ProductRepository;
import com.shstat.response.AddProductApiResponse;
import com.shstat.response.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.shstat.AttrMapper.mappingError;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository;
    private static final List<AttrFieldMappingVal<Field>> commonProductProperties;
    private static final List<AttrFieldMappingVal<Field>> productPerDateAttributeProperties;
    @PersistenceContext
    private EntityManager entityManager;
    private static final String SAVE_PRODUCT_QUEUE = "SAVE_PRODUCT_QUEUE";
    @Autowired
    private JmsTemplate queue;

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

                                return mappingError("Categories");
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
                                return BigDecimal.valueOf(Double.parseDouble(val.toString()));
                            }),
                    AttrFieldMappingVal.of("Date", ProductBasedOnDateAttributes.class.getDeclaredField("scrapDate"),
                            (val) -> {
                                if (val instanceof Long) {
                                    return new Date((Long) val);
                                } else if (val instanceof Date) {
                                    return val;
                                }

                                return mappingError("Date");
                            }),
                    AttrFieldMappingVal.of("Formatted Date", ProductBasedOnDateAttributes.class.getDeclaredField("formattedScrapDate"))
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductService(ProductRepository productRepository, ProductBasedOnDateAttributesRepository productBasedOnDateAttributesRepository) {
        this.productRepository = productRepository;
        this.productBasedOnDateAttributesRepository = productBasedOnDateAttributesRepository;
    }

    public void addProductsAsync(List<Map<String, Object>> products) {
        queue.convertAndSend(SAVE_PRODUCT_QUEUE, products);
    }

    @JmsListener(destination = SAVE_PRODUCT_QUEUE)
    protected void addProductsListener(List<Map<String, Object>> products) {
        for (int i = 0; i < products.size(); i += 5) {
            List<Map<String, Object>> partialList = products.subList(i, 5);
            ApiResponse apiResponse = addProducts(partialList);
            System.out.println(apiResponse.getMessages());
        }
    }

    public ApiResponse addProducts(List<Map<String, Object>> products) {
        List<Product> allMapped = new LinkedList<>();
        List<String> messages = new LinkedList<>();
        int i = 1;
        for (Map<String, Object> product : products) {
            try {
                Product mappedProduct = mapProduct(product);
                mappedProduct = productRepository.save(mappedProduct);
                allMapped.add(mappedProduct);
            } catch (MapperException e) {
                System.err.println(e.getMessage());
                if (e.isSendErrorMessage()) {
                    messages.add(e.getMessage());
                }
            }
            i++;
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
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof LocalDateTime) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof Number) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
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
                    throw new RuntimeException("Not implemented for: " + attrs.toString());
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

    private static <T> Optional<T> findProductAttr(Product product, String key, Object value, Class<T> productAttrClass) {
        return (Optional<T>) product.getAttributes().stream().filter(e -> e.getName().equals(key))
                .filter(e -> e.getClass().isAssignableFrom(productAttrClass))
                .filter(e -> {
                    try {
                        return e.getClass().getDeclaredMethod("getValue").invoke(e)
                                .equals(value);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).findFirst();
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
    public ApiResponse refreshMaterializedViews() {
        productRepository.refreshHistoricalLowPricesTable();
        productRepository.refreshLowerPricesThanHistoricalLowTable();
        productRepository.refreshLowerThanAVGForLastMonth();

        lowerThanAVGForLastMonth();

        //create indexes for category and shop
        return new ApiResponse(Collections.singletonList("Views refreshed."));
    }


    private void lowerThanAVGForLastMonth() {
        String createTableQuery = "CREATE OR REPLACE TABLE LOWER_THAN_AVG_FOR_X_MONTHS AS";
        String sqlFor1MonthOffset = getSql(1);
        String sqlFor3MonthOffset = getSql(3);
        String sqlFor6MonthOffset = getSql(6);
        String sqlFor12MonthOffset = getSql(12);
        entityManager.createNativeQuery(createTableQuery + sqlFor1MonthOffset).executeUpdate();
        String insertIntoQuery = "INSERT INTO LOWER_THAN_AVG_FOR_X_MONTHS";
        entityManager.createNativeQuery(insertIntoQuery + sqlFor3MonthOffset).executeUpdate();
        entityManager.createNativeQuery(insertIntoQuery + sqlFor6MonthOffset).executeUpdate();
        entityManager.createNativeQuery(insertIntoQuery + sqlFor12MonthOffset).executeUpdate();
    }

    private static String getSql(int months) {
        return " WITH RankedPrices AS (SELECT DISTINCT product_id, AVG(price) AS average_price FROM scrapdb.product_based_on_date_attributes AS pda " +
                " WHERE price <> -1 AND MONTH(pda.scrap_date) > MONTH(CURRENT_DATE - INTERVAL " + months + " MONTH) GROUP BY product_id)" +
                " SELECT DISTINCT pda.id AS id, pda.scrap_date AS scrap_date, pda.price AS price, rp.average_price AS avgPrice, " + months + " AS month_offset, " +
                """
                                p.name AS product_name, p.shop as shop, pc.categories AS category, p.img_src AS product_image_src,
                                (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) AS discount_in_percent
                        FROM scrapdb.product_based_on_date_attributes pda
                                JOIN scrapdb.product p ON p.id = pda.product_id
                                JOIN RankedPrices rp ON p.id = rp.product_id
                                LEFT JOIN scrapdb.product_categories pc ON pda.product_id = pc.product_id
                        WHERE scrap_date >= DATE_SUB(CURDATE(), INTERVAL 2 DAY)
                                AND scrap_date < CURDATE()
                                AND pda.price < rp.average_price
                                AND pda.scrap_date in (SELECT MAX(scrap_date)
                                                        FROM scrapdb.product_based_on_date_attributes AS pda1
                                                        WHERE price <>-1
                                                        AND pda.id = pda1.id)
                                ORDER BY pda.id;
                        """;
    }

    public List<Product> findAllByIds(Collection<Long> ids) {
        return productRepository.findAllById(ids);
    }
}
