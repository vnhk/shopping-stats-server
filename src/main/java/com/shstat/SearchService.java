package com.shstat;

import com.shstat.entity.Product;
import com.shstat.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class SearchService {
    @Autowired
    private ProductRepository productRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public Page<Product> findProductsByCategory(String category, String shop, Pageable pageable) {
        if (shop == null || shop.isBlank()) {
            return productRepository.findProductsByCategoriesIn(category, pageable);
        }
        return productRepository.findProductsByCategoriesInAndShop(category, shop, pageable);
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

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, String category, String shop, String name) {
        return productRepository.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin, discountMax, months, category, shop, name);
    }

    public Page<Product> findProducts(String name, Pageable pageable) {
        return productRepository.findByNameContaining(name, pageable);
    }

    public Product findProductByProductBasedOnDateAttributesId(Long id) {
        return productRepository.findProductByProductBasedOnDateAttributesId(id);
    }

    public Page<Product> findById(Long id, Pageable pageable) {
        Optional<Product> byId = productRepository.findById(id);
        if (byId.isPresent()) {
            return new PageImpl(Collections.singletonList(byId), pageable, 1);
        } else {
            return new PageImpl(Collections.emptyList(), pageable, 0);
        }
    }
}
