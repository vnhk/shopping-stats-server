package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {
    Optional<Product> findByNameAndShopAndProductListNameAndProductListUrlAndOfferUrl(String name,
                                                                                      String shop,
                                                                                      String productListName,
                                                                                      String productListUrl,
                                                                                      String offerUrl);

    @Query(value = """
            SELECT DISTINCT p FROM Product p JOIN ActualProduct ap ON ap.productId = p.id
                JOIN ProductBasedOnDateAttributes pda ON pda.product = p 
                WHERE pda.scrapDate = (SELECT MAX(pda1.scrapDate) FROM ProductBasedOnDateAttributes pda1 WHERE pda1.product = p)
                AND pda.price > 0
                AND p.name LIKE %:name%
            """)
    Page<Product> findWithName(String name, Pageable pageable);

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM LOWER_THAN_AVG_FOR_X_MONTHS
                    WHERE (category IN (:categories))
                        AND shop = COALESCE(:shop, shop)
                        AND month_offset = :months
                        AND avgPrice >= :prevPriceMin AND avgPrice <= :prevPriceMax
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND (:name IS NULL OR product_name LIKE CONCAT('%', :name, '%'))
                    ORDER BY discount_in_percent DESC
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, List<String> categories, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);


    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM LOWER_THAN_AVG_FOR_X_MONTHS
                    WHERE shop = COALESCE(:shop, shop)
                        AND month_offset = :months
                        AND avgPrice >= :prevPriceMin AND avgPrice <= :prevPriceMax
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND (:name IS NULL OR product_name LIKE CONCAT('%', :name, '%'))
                    ORDER BY discount_in_percent DESC
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);


    @Query(nativeQuery = true, value =
            """
                    SELECT id, scrap_date, price
                    FROM HISTORICAL_LOW_PRICES_TABLE
                    WHERE categories = COALESCE(:category, categories)
                        AND shop = COALESCE(:shop, shop)
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> historicalLowPriceProducts(Pageable pageable, String category, String shop, String name);

    Product findProductByProductBasedOnDateAttributesId(Long id);

    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE " +
            " c = COALESCE(:category, c) AND p.shop = COALESCE(:shop, p.shop) AND p.name LIKE %:productName%")
    Page<Product> findProductsBy(String category, String shop, String productName, Pageable pageable);

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM LOWER_PRICES_THAN_HISTORICAL_LOW
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND historical_low_price >= COALESCE(:prevPriceMin ,historical_low_price) AND historical_low_price <= COALESCE(:prevPriceMax ,historical_low_price)
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                    ORDER BY discount_in_percent DESC;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findAllXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM LOWER_PRICES_THAN_HISTORICAL_LOW
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND historical_low_price >= COALESCE(:prevPriceMin ,historical_low_price) AND historical_low_price <= COALESCE(:prevPriceMax ,historical_low_price)
                        AND scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 2 DAY)
                                         AND scrap_date < CURRENT_TIMESTAMP()
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                    ORDER BY discount_in_percent DESC;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findActualXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);

    @Query(value = "SELECT DISTINCT p.categories FROM Product p")
    Set<String> findCategories();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE HISTORICAL_LOW_PRICES_TABLE AS
            SELECT pda.id AS id, pda.scrap_date, pda.price, p.name as product_name, p.shop, pc.categories, p.img_src as product_image_src
            FROM product AS p
                     JOIN product_categories pc ON p.id = pc.product_id
                     JOIN product_based_on_date_attributes AS pda ON p.id = pda.product_id
                     JOIN actual_product ap ON ap.product_id = pda.product_id AND ap.scrap_date = pda.scrap_date
                     JOIN product_stats ps ON ps.product_id = pda.product_id AND ps.historical_low = pda.price
            ORDER BY pda.id;
            """, nativeQuery = true)
    @Transactional
    void refreshHistoricalLowPricesTable();

    @Modifying
    @Query(value = """
                        CREATE OR REPLACE TABLE LOWER_PRICES_THAN_HISTORICAL_LOW AS
                        WITH RankedPrices AS (SELECT product_id, MIN(price) as min_price
                                              FROM product_based_on_date_attributes pda
                                              WHERE (product_id, price) NOT IN
                                                    (SELECT product_id, MIN(price)
                                                     FROM product_based_on_date_attributes pda
                                                     WHERE pda.price <> -1
                                                     GROUP BY product_id)
                                                AND pda.price <> -1
                                              GROUP BY product_id)
                        SELECT DISTINCT pda.id                              AS id,
                                        pda.scrap_date                      AS scrap_date,
                                        pda.price                           AS price,
                                        rp1.min_price                       AS historical_low_price,
                                        (IF(pda.price >= rp1.min_price, 0, (1 - pda.price / rp1.min_price) * 100))   AS discount_in_percent,
                                        p.shop                              AS shop,
                                        p.img_src                           AS product_image_src,
                                        p.name                              AS product_name,
                                        pc.categories                       AS category,
                                        pda.product_id                      AS product_id
                        FROM product_based_on_date_attributes pda
                                 JOIN RankedPrices rp1 ON pda.product_id = rp1.product_id
                                 JOIN product p ON p.id = pda.product_id
                                 LEFT JOIN product_categories pc ON pda.product_id = pc.product_id
                                 JOIN actual_product ap ON ap.product_id = pda.product_id AND ap.scrap_date = pda.scrap_date
                        WHERE pda.price > 0
                          AND pda.price <= rp1.min_price
                        ORDER BY pda.id;
            """, nativeQuery = true)
    @Transactional
    void refreshLowerPricesThanHistoricalLowTable();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE LOWER_THAN_AVG_FOR_LAST_MONTH AS
                                    WITH RankedPrices AS (SELECT product_id, AVG(price) AS average_price
                                                          FROM product_based_on_date_attributes AS pda
                                                          WHERE price <> -1
                                                            AND MONTH(pda.scrap_date) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH)
                                                          GROUP BY product_id)
                                    SELECT DISTINCT pda.id                                                                           AS id,
                                                    pda.scrap_date                                                                   as scrap_date,
                                                    pda.price                                                                        as price,
                                                    rp.average_price                                                                 as avgPrice,
                                                    p.name                                                                           AS product_name,
                                                    p.shop                                                                           as shop,
                                                    pc.categories                                                                    AS category,
                                                    p.img_src                                                                        AS product_image_src,
                                                    (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) AS discount_in_percent
                                    FROM product_based_on_date_attributes pda
                                             JOIN product p ON p.id = pda.product_id
                                             JOIN RankedPrices rp ON p.id = rp.product_id
                                             LEFT JOIN product_categories pc ON pda.product_id = pc.product_id
                                    WHERE scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 2 DAY)
                                      AND scrap_date < CURRENT_TIMESTAMP()
                                      AND pda.price < rp.average_price
                                      AND pda.scrap_date in (SELECT MAX(scrap_date)
                                                             FROM product_based_on_date_attributes AS pda1
                                                             WHERE price <> -1
                                                               AND pda.id = pda1.id)
                                    ORDER BY pda.id;
            """, nativeQuery = true)
    @Transactional
    void refreshLowerThanAVGForLastMonth();

    interface ProductBasedOnDateAttributesNativeResInterface {
        Long getId();

        Date getScrapDate();

        BigDecimal getPrice();
    }

    class ProductBasedOnDateAttributesNativeRes implements ProductBasedOnDateAttributesNativeResInterface {
        Long id;
        Date scrapDate;
        BigDecimal price;

        public ProductBasedOnDateAttributesNativeRes(Long id, Date scrapDate, BigDecimal price) {
            this.id = id;
            this.scrapDate = scrapDate;
            this.price = price;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Date getScrapDate() {
            return scrapDate;
        }

        @Override
        public BigDecimal getPrice() {
            return price;
        }
    }
}
