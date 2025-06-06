package com.bervan.shstat.service;

import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductSearchService {
    @Autowired
    private ProductRepository productRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public Page<Product> findProducts(String category, String shop, String productName, Pageable pageable) {
        return productRepository.findProductsBy(category, shop, productName, pageable);
    }

    public Set<String> findCategories() {
        return productRepository.findCategories();
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findHistoricalLowProducts(Pageable pageable, String category, String shop, String name) {
        if (!(name == null || name.isBlank() || name.isEmpty())) {
            name = "%" + name + "%";
        }
        return productRepository.historicalLowPriceProducts(pageable, category, shop, name);
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop, boolean onlyActualOffers, String name, Integer prevPriceMin, Integer prevPriceMax) {
        if (!(name == null || name.isBlank() || name.isEmpty())) {
            name = "%" + name + "%";
        }
        if (onlyActualOffers) {
            return productRepository.findActualXPercentLowerPriceThanHistoricalLow(pageable, discountMin, discountMax, category, shop, name, prevPriceMin, prevPriceMax);
        } else {
            return productRepository.findAllXPercentLowerPriceThanHistoricalLow(pageable, discountMin, discountMax, category, shop, name, prevPriceMin, prevPriceMax);
        }
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, List<String> categories, String shop, String name, Integer prevPriceMin, Integer prevPriceMax) {
        if (name != null) {
            name = name.toUpperCase();
        }

        if (categories == null || categories.isEmpty()) {
            return productRepository.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin, discountMax, months, shop, name, prevPriceMin, prevPriceMax);
        } else {
            return productRepository.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin, discountMax, months, categories, shop, name, prevPriceMin, prevPriceMax);
        }
    }

    public Page<Product> findProducts(String name, Pageable pageable) {
        return productRepository.findWithName(name, pageable);
    }

    public Product findProductByProductBasedOnDateAttributesId(Long id) {
        return productRepository.findProductByProductBasedOnDateAttributesId(id);
    }

    public Page<Product> findById(Long id, Pageable pageable) {
        Optional<Product> byId = productRepository.findById(id);
        return byId.map(product -> new PageImpl(Collections.singletonList(product), pageable, 1)).orElseGet(() ->
                new PageImpl(Collections.emptyList(), pageable, 0));
    }
}
