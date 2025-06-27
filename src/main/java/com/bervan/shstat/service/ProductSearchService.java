package com.bervan.shstat.service;

import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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


    public Page<Product> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, List<String> categories, String shop, String name, Integer prevPriceMin, Integer prevPriceMax) {
        if (name != null) {
            name = name.toUpperCase();
        }

        return findBestOffers(categories, shop, Double.valueOf(prevPriceMin), Double.valueOf(prevPriceMax), discountMin, discountMax, name, months, pageable);
    }

    public Page<Product> findBestOffers(
            List<String> categories,
            String shop,
            Double prevPriceMin,
            Double prevPriceMax,
            Double discountMin,
            Double discountMax,
            String name,
            Integer months,
            Pageable pageable) {

        String discountColumn;
        switch (months) {
            case 1 -> discountColumn = "pbo.discount1month";
            case 2 -> discountColumn = "pbo.discount2month";
            case 3 -> discountColumn = "pbo.discount3month";
            case 6 -> discountColumn = "pbo.discount6month";
            case 12 -> discountColumn = "pbo.discount12month";
            default -> throw new IllegalArgumentException("Unsupported months: " + months);
        }

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT pbo.product_id as productId
                FROM product_best_offer pbo
                """);

        if (categories != null && !categories.isEmpty()) {
            sql.append("JOIN product_categories pc ON pbo.product_id = pc.product_id ");
        }

        sql.append("WHERE 1=1 ");

        if (categories != null && !categories.isEmpty()) {
            sql.append("AND pc.category IN :categories ");
        }

        sql.append("""
                AND pbo.shop = COALESCE(:shop, pbo.shop)
                AND pbo.price BETWEEN :prevPriceMin AND :prevPriceMax
                """);

        sql.append("AND ").append(discountColumn).append(" BETWEEN :discountMin AND :discountMax ");

        if (name != null && !name.isEmpty()) {
            sql.append("AND pbo.product_name LIKE CONCAT('%', :name, '%') ");
        }

        sql.append("ORDER BY " + discountColumn + " DESC");

        Query query = entityManager.createNativeQuery(sql.toString());

        if (categories != null && !categories.isEmpty()) {
            query.setParameter("categories", categories);
        }
        query.setParameter("shop", shop);
        query.setParameter("prevPriceMin", prevPriceMin);
        query.setParameter("prevPriceMax", prevPriceMax);
        query.setParameter("discountMin", discountMin);
        query.setParameter("discountMax", discountMax);
        if (name != null && !name.isEmpty()) {
            query.setParameter("name", name);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();

        List<Long> productIds = resultList.stream()
                .map(row ->
                        ((Number) row[0]).longValue())
                .toList();

        StringBuilder countSql = new StringBuilder("""
                SELECT COUNT(DISTINCT pbo.id)
                FROM product_best_offer pbo
                """);

        if (categories != null && !categories.isEmpty()) {
            countSql.append("JOIN product_categories pc ON pbo.product_id = pc.product_id ");
        }

        countSql.append("WHERE 1=1 ");

        if (categories != null && !categories.isEmpty()) {
            countSql.append("AND pc.category IN :categories ");
        }

        countSql.append("""
                AND pbo.shop = COALESCE(:shop, pbo.shop)
                AND pbo.price BETWEEN :prevPriceMin AND :prevPriceMax
                """);

        countSql.append("AND ").append(discountColumn).append(" BETWEEN :discountMin AND :discountMax ");

        if (name != null && !name.isEmpty()) {
            countSql.append("AND pbo.product_name LIKE CONCAT('%', :name, '%') ");
        }

        Query countQuery = entityManager.createNativeQuery(countSql.toString());

        if (categories != null && !categories.isEmpty()) {
            countQuery.setParameter("categories", categories);
        }
        countQuery.setParameter("shop", shop);
        countQuery.setParameter("prevPriceMin", prevPriceMin);
        countQuery.setParameter("prevPriceMax", prevPriceMax);
        countQuery.setParameter("discountMin", discountMin);
        countQuery.setParameter("discountMax", discountMax);
        if (name != null && !name.isEmpty()) {
            countQuery.setParameter("name", name);
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<Product> content = productRepository.findAllById(productIds);

        return new PageImpl<>(content, pageable, total);
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
