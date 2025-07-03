package com.bervan.shstat.service;

import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import com.bervan.shstat.tokens.ProductTokensRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTokensRepository productTokensRepository;

    @Autowired
    private ProductSimilarOffersService productSimilarOffersService;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Product> findProducts(String category, String shop, String productName, Pageable pageable) {
        return productRepository.findProductsBy(category, shop, productName, pageable);
    }

    public Page<Product> findProductsByTokens(String category, String shop, String productName, Pageable pageable) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<Long>>> futures = new ArrayList<>();

        // Split product name into parts and process each part in parallel
        String[] productNameParts = productName.split(" ");
        for (String part : productNameParts) {
            futures.add(executor.submit(() -> {
                Set<String> tokens = productSimilarOffersService.buildNameTokens(part);
                List<Object[]> byTokens = productTokensRepository.findByTokens(tokens, Pageable.ofSize(1000000000), -1L);
                return byTokens.stream().map(e -> (Long) e[0]).toList();
            }));
        }

        // Wait for all tasks to finish and collect results
        List<Set<Long>> productIdSets = new ArrayList<>();
        for (Future<List<Long>> future : futures) {
            try {
                productIdSets.add(new HashSet<>(future.get()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        // Intersect IDs to find common ones in ALL productNameParts
        Set<Long> commonProductIds = productIdSets.get(0);
        for (int i = 1; i < productIdSets.size(); i++) {
            commonProductIds.retainAll(productIdSets.get(i));
        }

        // Process category if present
        if (category != null && !category.isEmpty()) {
            Set<String> categoryTokens = productSimilarOffersService.buildCategoryTokens(List.of(category));
            List<Object[]> categoryResults = productTokensRepository.findByTokens(categoryTokens, Pageable.ofSize(1000000000), -1L);
            Set<Long> categoryProductIds = categoryResults.stream().map(e -> (Long) e[0]).collect(Collectors.toSet());
            commonProductIds.retainAll(categoryProductIds);
        }

        return productRepository.findByShopAndIdsIn(commonProductIds, shop, pageable);
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

        String discountColumn = switch (months) {
            case 1 -> "pbo.discount1month";
            case 2 -> "pbo.discount2month";
            case 3 -> "pbo.discount3month";
            case 6 -> "pbo.discount6month";
            case 12 -> "pbo.discount12month";
            default -> throw new IllegalArgumentException("Unsupported months: " + months);
        };

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT pbo.product_id AS productId
                FROM product_best_offer pbo
                """);

        if (categories != null && !categories.isEmpty()) {
            sql.append("JOIN product_categories pc ON pbo.product_id = pc.product_id ");
        }

        sql.append("WHERE 1=1 ");

        if (categories != null && !categories.isEmpty()) {
            sql.append("AND pc.categories IN :categories ");
        }

        sql.append("""
                AND pbo.shop = COALESCE(:shop, pbo.shop)
                AND pbo.price BETWEEN :prevPriceMin AND :prevPriceMax
                """);

        sql.append("AND ").append(discountColumn).append(" BETWEEN :discountMin AND :discountMax ");

        if (name != null && !name.isEmpty()) {
            sql.append("AND pbo.product_name LIKE CONCAT('%', :name, '%') ");
        }

        sql.append("ORDER BY ").append(discountColumn).append(" DESC");

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
        List<Number> resultList = query.getResultList();

        List<Long> productIds = resultList.stream()
                .map(Number::longValue)
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
            countSql.append("AND pc.categories IN :categories ");
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
        return byId.map(product -> new PageImpl<>(Collections.singletonList(product), pageable, 1))
                .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
    }
}