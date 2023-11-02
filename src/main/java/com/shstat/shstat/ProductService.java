package com.shstat.shstat;

import com.shstat.shstat.entity.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.shstat.shstat.AttrMapper.mappingError;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final Map<String, ? extends ProductAttributeRepository> repositories;
    private static final List<AttrFieldMappingVal<Field>> commonProductProperties;
    private static final List<AttrFieldMappingVal<Field>> productPerDateAttributeProperties;

    static {
        try {
            commonProductProperties = List.of(
                    AttrFieldMappingVal.of("Name", Product.class.getDeclaredField("name")),
                    AttrFieldMappingVal.of("Product List Url", Product.class.getDeclaredField("productListUrl")),
                    AttrFieldMappingVal.of("Shop", Product.class.getDeclaredField("shop")),
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
                    AttrFieldMappingVal.of("Name", ProductBasedOnDateAttributes.class.getDeclaredField("price")),
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

    public ProductService(ProductRepository productRepository,
                          Map<String, ? extends ProductAttributeRepository> repositories) {
        this.productRepository = productRepository;
        this.repositories = repositories;
    }

    public ApiResponse addProducts(List<Map<String, Object>> products) {
        List<Product> allMapped = new ArrayList<>();
        int i = 1;
        for (Map<String, Object> product : products) {
            Product mappedProduct = mapProduct(product);
            System.out.println("Product " + i + " mapped. " + (products.size() - i) + " products left.");
            i++;
            allMapped.add(mappedProduct);
        }

        productRepository.saveAll(allMapped);

        return new AddProductApiResponse(new ArrayList<>(), allMapped.size());
    }

    private Product mapProduct(Map<String, Object> product) {
        Product res = mapProductCommonAttr(product);
        ProductBasedOnDateAttributes perDateAttributes = mapProductPerDateAttributes(product);
        res.getPerDateAttributes().add(perDateAttributes);
        Set<ProductAttribute> resAttributes = new HashSet<>();
        for (Map.Entry<String, Object> attrs : product.entrySet()) {
            String key = attrs.getKey();
            Object value = attrs.getValue();
            if (value instanceof Date) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof String) {
                Optional attrOpt = repositories.get(ProductTextAttribute.class.getName())
                        .findByProductAndNameAndValue(res, key, value);
                if (attrOpt.isEmpty()) {
                    resAttributes.add(new ProductTextAttribute(key, value.toString()));
                } else {
                    resAttributes.add((ProductAttribute) attrOpt.get());
                }
            } else if (value instanceof LocalDate) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof LocalDateTime) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof Number) {
                Optional attrOpt = repositories.get(ProductNumberAttribute.class.getName())
                        .findByProductAndNameAndValue(res, key, value);
                if (attrOpt.isEmpty()) {
                    resAttributes.add(new ProductNumberAttribute(key, (Number) value));
                } else {
                    resAttributes.add((ProductAttribute) attrOpt.get());
                }
            } else if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof String) {
                    Optional attrOpt = repositories.get(ProductListTextAttribute.class.getName())
                            .findByProductAndName(res, key);
                    if (attrOpt.isPresent()) {
                        ProductListTextAttribute attr = (ProductListTextAttribute) attrOpt.get();
                        attr.getValue().addAll((List<String>) value);
                        resAttributes.add(attr);
                    } else {
                        resAttributes.add(new ProductListTextAttribute(key, new HashSet<>((List<String>) value)));
                    }
                } else if (!list.isEmpty() && list.get(0) instanceof Number) {
                    throw new RuntimeException("Not implemented for: " + attrs.toString());
                }
            } else if (value instanceof String[]) {
                Optional attrOpt = repositories.get(ProductListTextAttribute.class.getName())
                        .findByProductAndName(res, key);
                if (attrOpt.isPresent()) {
                    ProductListTextAttribute attr = (ProductListTextAttribute) attrOpt.get();
                    attr.getValue().addAll(List.of((String[]) value));
                    resAttributes.add(attr);
                } else {
                    resAttributes.add(new ProductListTextAttribute(key, new HashSet<>(List.of((String[]) value))));
                }
            } else if (value != null) {
                Optional attrOpt = repositories.get(ProductTextAttribute.class.getName())
                        .findByProductAndNameAndValue(res, key, value);
                if (attrOpt.isEmpty()) {
                    resAttributes.add(new ProductTextAttribute(key, value.toString()));
                } else {
                    resAttributes.add((ProductAttribute) attrOpt.get());
                }
            }
        }

        res.setAttributes(resAttributes);
        return res;
    }

    private ProductBasedOnDateAttributes mapProductPerDateAttributes(Map<String, Object> product) {
        ProductBasedOnDateAttributes res = new ProductBasedOnDateAttributes();
        for (AttrFieldMappingVal<Field> perDateAttrs : productPerDateAttributeProperties) {
            try {
                Object value = product.get(perDateAttrs.attr);
                Field field = perDateAttrs.val;
                field.setAccessible(true);
                value = perDateAttrs.mapper.map(value);
                field.set(res, value);
                field.setAccessible(false);
                product.remove(perDateAttrs.attr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return res;
    }

    private Product findProductBasedOnAttributes(Product res) {
        Optional<Product> product = productRepository.findByNameAndShopAndProductListNameAndProductListUrl(res.getName(), res.getShop(),
                res.getProductListName(), res.getProductListUrl());
        return product.orElse(res);
    }

    private Product mapProductCommonAttr(Map<String, Object> product) {
        Product res = new Product();
        for (AttrFieldMappingVal<Field> commonProductProperty : commonProductProperties) {
            try {
                Object value = product.get(commonProductProperty.attr);
                Field field = commonProductProperty.val;
                field.setAccessible(true);
                value = commonProductProperty.mapper.map(value);
                field.set(res, value);
                field.setAccessible(false);
                product.remove(commonProductProperty.attr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return findProductBasedOnAttributes(res);
    }
}
