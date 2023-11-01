package com.shstat.shstat;

import com.shstat.shstat.entity.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private static List<AttrVal<Field>> commonProductProperties;

    static {
        try {
            commonProductProperties = List.of(AttrVal.of("Name", Product.class.getDeclaredField("name")), AttrVal.of("Price", Product.class.getDeclaredField("price")));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ApiResponse addProducts(List<Map<String, Object>> products) {
        List<Product> allMapped = new ArrayList<>();
        for (Map<String, Object> product : products) {
            Product mappedProduct = map(product);
            allMapped.add(mappedProduct);
        }

        productRepository.saveAll(allMapped);

        return new AddProductApiResponse(new ArrayList<>(), allMapped.size());
    }

    private Product map(Map<String, Object> product) {
        Product res = new Product();
        for (AttrVal<Field> commonProductProperty : commonProductProperties) {
            try {
                Object value = product.get(commonProductProperty.attr);
                Field field = commonProductProperty.val;
                field.setAccessible(true);
                field.set(res, value);
                field.setAccessible(false);
                product.remove(commonProductProperty.attr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Set<ProductAttribute> resAttributes = new HashSet<>();
        for (Map.Entry<String, Object> attrs : product.entrySet()) {
            String key = attrs.getKey();
            Object value = attrs.getValue();
            if (value instanceof Date) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof String) {
                resAttributes.add(new ProductTextAttribute(key, value.toString()));
            } else if (value instanceof LocalDate) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof LocalDateTime) {
                throw new RuntimeException("Not implemented for: " + attrs.toString());
            } else if (value instanceof Number) {
                resAttributes.add(new ProductNumberAttribute(key, (Number) value));
            } else if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof String) {
                    resAttributes.add(new ProductListTextAttribute(key, (List<String>) value));
                } else if (!list.isEmpty() && list.get(0) instanceof Number) {
                    throw new RuntimeException("Not implemented for: " + attrs.toString());
                }
            } else if (value instanceof String[]) {
                resAttributes.add(new ProductListTextAttribute(key, List.of((String[]) value)));
            } else if (value != null) {
                resAttributes.add(new ProductTextAttribute(key, value.toString()));
            }
        }

        res.setAttributes(resAttributes);
        return res;
    }
}
