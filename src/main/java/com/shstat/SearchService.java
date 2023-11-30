package com.shstat;

import com.shstat.entity.Product;
import com.shstat.repository.ProductRepository;
import com.shstat.response.ApiResponse;
import com.shstat.response.SearchApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SearchService {
    @Autowired
    private ProductRepository productRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public ApiResponse getProducts(String categories, String name, String shop, Integer priceMin, Integer priceMax) {
        if (Strings.isBlank(categories) || Strings.isBlank(name) || Strings.isBlank(shop) || priceMin == null
                || priceMax == null) {
            throw new RuntimeException("At least one search parameter is required!");
        }
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM PRODUCT p WHERE 1=1 "
//                        categories(categories) +
//                        shop(categories) +
//                        name(categories) +
//                        priceMin(categories) +
//                        priceMax(categories)
                , Product.class);

        List<Product> resultList = query.getResultList();
        return SearchApiResponse.builder()
                .build();
    }

    public Set<String> findProductNames(String shop) {
        return productRepository.findProductNames(shop);
    }

    public List<Product> findProducts(String name, String shop) {
        return productRepository.findByNameContainingAndShop(name, shop);
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeRes> findHistoricalLowProducts(Pageable pageable) {
        return productRepository.historicalLowPriceProducts(pageable);
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeRes> find10PercentLowerPriceThanHistoricalLow(Pageable pageable) {
        return productRepository.find10PercentLowerPriceThanHistoricalLow(pageable);
    }

    public List<Product> findProducts(String name) {
        return productRepository.findByNameContaining(name);
    }

    public Product findProductByProductBasedOnDateAttributesId(Long id) {
        return productRepository.findProductByProductBasedOnDateAttributesId(id);
    }
}
