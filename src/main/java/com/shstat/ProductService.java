package com.shstat;

import com.shstat.entity.Product;
import com.shstat.entity.ProductAttribute;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.entity.ProductListTextAttribute;
import com.shstat.repository.ProductBasedOnDateAttributesRepository;
import com.shstat.repository.ProductRepository;
import com.shstat.response.AddProductApiResponse;
import com.shstat.response.ApiResponse;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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

    public ApiResponse addProducts(List<Map<String, Object>> products) {
        List<Product> allMapped = new ArrayList<>();
        List<String> messages = new ArrayList<>();
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
//            System.out.println("Product " + i + " mapped. " + (products.size() - i) + " products left.");
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
                throw new RuntimeException("Not implemented for: " + attrs.toString());
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
            if (res.getImgSrc() != null) {
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
}
