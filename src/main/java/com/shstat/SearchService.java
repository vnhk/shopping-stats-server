package com.shstat;

import com.shstat.entity.Product;
import com.shstat.repository.ProductRepository;
import com.shstat.response.ApiResponse;
import com.shstat.response.SearchApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    public Page<Product> findProductsByCategory(String category, String shop, Pageable pageable) {
        if (shop == null || shop.isBlank()) {
            return productRepository.findProductsByCategoriesIn(category, pageable);
        }
        return productRepository.findProductsByCategoriesInAndShop(category, shop, pageable);
    }

    public List<Product> findProducts(String name, String shop) {
        return productRepository.findByNameContainingAndShop(name, shop);
    }

    public Set<String> findCategories() {
        return productRepository.findCategories();
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findHistoricalLowProducts(Pageable pageable, String category, String shop) {
        return productRepository.historicalLowPriceProducts(pageable, category, shop);
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discount, String category, String shop, boolean onlyActualOffers) {
        if (onlyActualOffers) {
            return productRepository.findActualXPercentLowerPriceThanHistoricalLow(pageable, discount, category, shop);
        } else {
            return productRepository.findAllXPercentLowerPriceThanHistoricalLow(pageable, discount, category, shop);
        }
    }

    public Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discount, Integer months, String category, String shop) {
        LocalDate beginDateForCalculatingAVG = LocalDate.now().minusMonths(months);

        Query query = entityManager.createNativeQuery(findXPercentLowerPriceThanHistoricalLowQuery2(false), Object[].class);
        Query countQuery = entityManager.createNativeQuery(findXPercentLowerPriceThanHistoricalLowQuery2(true), Long.class);
        countQuery.setParameter("discount", discount);
        countQuery.setParameter("scrap_date_begin", beginDateForCalculatingAVG.toString());
        query.setParameter("discount", discount);
        query.setParameter("scrap_date_begin", beginDateForCalculatingAVG.toString());
        countQuery.setParameter("category", category);
        query.setParameter("category", category);
        countQuery.setParameter("shop", shop);
        query.setParameter("shop", shop);
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        query.setFirstResult((pageNumber) * pageSize);
        query.setMaxResults(pageSize);
        List<Object[]> objs = query.getResultList();
        List<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> res = new ArrayList<>();
        for (Object[] obj : objs) {
            res.add(new ProductRepository.ProductBasedOnDateAttributesNativeRes((Long) obj[0], (Date) obj[1], (BigDecimal) obj[2]));
        }

        return new PageImpl<>(res, pageable, (Long) countQuery.getResultList().get(0));
    }

    private static String findXPercentLowerPriceThanHistoricalLowQuery2(boolean countQuery) {
        return """
                SELECT
                """ + (countQuery ? "count(pda.id)" : """
                                DISTINCT
                                pda.id AS id,
                                pda.scrap_date AS scrap_date,
                                pda.price AS price
                """)
                +
                """
                        FROM scrapdb.product_based_on_date_attributes pda
                        JOIN scrapdb.product p ON p.id = pda.product_id
                        LEFT JOIN scrapdb.product_categories pc ON pda.product_id = pc.product_id
                        WHERE (:category IS NULL OR :category = pc.categories)
                          AND p.shop = COALESCE(:shop, p.shop) AND pda.price / :discount <= (
                            SELECT AVG(price) AS average_price
                                  FROM scrapdb.product_based_on_date_attributes AS pda1
                                  WHERE price <> -1
                                      AND pda.id = pda1.id
                                      AND scrap_date >= :scrap_date_begin)
                          AND pda.scrap_date in
                          (SELECT MAX(scrap_date)
                                 FROM scrapdb.product_based_on_date_attributes AS pda1
                                 WHERE price <> -1
                                      AND pda.id = pda1.id)
                        ORDER BY pda.id;
                        """;
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
