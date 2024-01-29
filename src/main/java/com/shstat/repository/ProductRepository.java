package com.shstat.repository;

import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndShopAndProductListNameAndProductListUrl(String name,
                                                                           String shop,
                                                                           String productListName,
                                                                           String productListUrl);

    List<Product> findByNameContainingAndShop(String name, String shop);

    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE :category = c")
    Page<Product> findProductsByCategoriesIn(String category, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE :category = c AND p.shop = :shop")
    Page<Product> findProductsByCategoriesInAndShop(String category, String shop, Pageable pageable);

    @Query(nativeQuery = true, value =
            """
                    SELECT id, scrap_date, price
                    FROM scrapdb.HISTORICAL_LOW_PRICES_TABLE
                    WHERE categories = COALESCE(:category, categories)
                        AND shop = COALESCE(:shop, shop)
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> historicalLowPriceProducts(Pageable pageable, String category, String shop, String name);

    Product findProductByProductBasedOnDateAttributesId(Long id);

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM scrapdb.LOWER_PRICES_THAN_HISTORICAL_LOW
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND historical_low_price >= COALESCE(:prevPriceMin ,historical_low_price) AND historical_low_price <= COALESCE(:prevPriceMax ,historical_low_price)
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                        AND product_image_src is not null AND product_image_src <> '' AND TRIM(product_image_src) <> '' AND LENGTH(product_image_src) >= 10
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findAllXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM scrapdb.LOWER_PRICES_THAN_HISTORICAL_LOW
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND historical_low_price >= COALESCE(:prevPriceMin ,historical_low_price) AND historical_low_price <= COALESCE(:prevPriceMax ,historical_low_price)
                        AND product_image_src is not null AND product_image_src <> '' AND TRIM(product_image_src) <> '' AND LENGTH(product_image_src) >= 10
                        AND scrap_date >= DATE_SUB(CURDATE(), INTERVAL 2 DAY)
                                         AND scrap_date < CURDATE()
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findActualXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop, String name, Integer prevPriceMin, Integer prevPriceMax);

    @Query(value = "SELECT DISTINCT p.categories FROM Product p")
    Set<String> findCategories();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE HISTORICAL_LOW_PRICES_TABLE AS
            SELECT pda.id AS id, pda.scrap_date, pda.price, p.name as product_name, p.shop, pc.categories, p.img_src as product_image_src
            FROM scrapdb.product AS p
                     JOIN scrapdb.product_categories pc ON p.id = pc.product_id
                     JOIN scrapdb.product_based_on_date_attributes AS pda ON p.id = pda.product_id
                     JOIN (SELECT product_id, MAX(scrap_date) AS max_date
                           FROM scrapdb.product_based_on_date_attributes
                           WHERE price <> -1
                           GROUP BY product_id) AS latest_dates
                          ON pda.product_id = latest_dates.product_id AND pda.scrap_date = latest_dates.max_date
            WHERE (pda.product_id, pda.price)
                IN (SELECT product_id, MIN(price) AS min_price
                    FROM scrapdb.product_based_on_date_attributes
                    WHERE price <> -1
                    GROUP BY product_id)
              AND (pda.product_id, pda.price) NOT IN (SELECT product_id, MAX(price) AS max_price
                                                      FROM scrapdb.product_based_on_date_attributes
                                                      WHERE price <> -1
                                                      GROUP BY product_id)
            ORDER BY pda.id;
            """, nativeQuery = true)
    void refreshHistoricalLowPricesTable();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE LOWER_THAN_AVG_FOR_LAST_MONTH AS
                                    WITH RankedPrices AS (SELECT product_id, AVG(price) AS average_price
                                                          FROM scrapdb.product_based_on_date_attributes AS pda
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
                                    FROM scrapdb.product_based_on_date_attributes pda
                                             JOIN scrapdb.product p ON p.id = pda.product_id
                                             JOIN RankedPrices rp ON p.id = rp.product_id
                                             LEFT JOIN scrapdb.product_categories pc ON pda.product_id = pc.product_id
                                    WHERE scrap_date >= DATE_SUB(CURDATE(), INTERVAL 2 DAY)
                                      AND scrap_date < CURDATE()
                                      AND pda.price < rp.average_price
                                      AND pda.scrap_date in (SELECT MAX(scrap_date)
                                                             FROM scrapdb.product_based_on_date_attributes AS pda1
                                                             WHERE price <> -1
                                                               AND pda.id = pda1.id)
                                    ORDER BY pda.id;
            """, nativeQuery = true)
    void refreshLowerThanAVGForLastMonth();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE LOWER_PRICES_THAN_HISTORICAL_LOW AS
            WITH RankedPrices AS (SELECT product_id, MIN(price) as min_price
                                  FROM scrapdb.product_based_on_date_attributes pda
                                  WHERE (product_id, price) NOT IN
                                        (SELECT product_id, MIN(price)
                                         FROM scrapdb.product_based_on_date_attributes pda
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
            FROM scrapdb.product_based_on_date_attributes pda
                     JOIN RankedPrices rp1 ON pda.product_id = rp1.product_id
                     JOIN scrapdb.product p ON p.id = pda.product_id
                     LEFT JOIN scrapdb.product_categories pc ON pda.product_id = pc.product_id
            WHERE pda.price <> 0
              AND pda.price <= rp1.min_price
              AND pda.scrap_date in
                  (SELECT MAX(scrap_date)
                   FROM scrapdb.product AS p
                            JOIN scrapdb.product_based_on_date_attributes AS pda ON p.id = pda.product_id
                   WHERE price <> -1
                     AND rp1.product_id = pda.product_id)
            ORDER BY pda.id;
                        """, nativeQuery = true)
    void refreshLowerPricesThanHistoricalLowTable();

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT id, scrap_date as scrapDate, price
                    FROM scrapdb.LOWER_THAN_AVG_FOR_X_MONTHS
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND month_offset = :months
                        AND discount_in_percent >= :discountMin AND discount_in_percent <= :discountMax
                        AND UPPER(product_name) LIKE UPPER(COALESCE(:name, product_name))
                        AND product_image_src is not null AND product_image_src <> '' AND TRIM(product_image_src) <> '' AND LENGTH(product_image_src) >= 10
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, String category, String shop, String name);

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

    @Query(value = "SELECT DISTINCT new ProductBasedOnDateAttributes(pd.price, pd.scrapDate, pd.formattedScrapDate) " +
            " FROM Product p " +
            "   JOIN ProductBasedOnDateAttributes pd ON pd.product = p " +
            " WHERE p.shop = :shop " +
            "   AND p.name = :name " +
            "   AND pd.price = " +
            "     (SELECT min(pd.price) as price " +
            "          FROM Product p " +
            "             JOIN ProductBasedOnDateAttributes pd ON pd.product = p " +
            "          WHERE p.shop = :shop " +
            "             AND pd.price <> -1" +
            "             AND p.name = :name)" +
            "   ORDER BY pd.scrapDate DESC LIMIT 1")
    ProductBasedOnDateAttributes lastMinPrice(String name, String shop);

    @Query(value = "SELECT DISTINCT new ProductBasedOnDateAttributes(pd.price, pd.scrapDate, pd.formattedScrapDate) " +
            " FROM Product p " +
            "   JOIN ProductBasedOnDateAttributes pd ON pd.product = p " +
            " WHERE p.shop = :shop " +
            "   AND p.name = :name " +
            "   AND pd.price = " +
            "     (SELECT max(pd.price) as price " +
            "          FROM Product p " +
            "             JOIN ProductBasedOnDateAttributes pd ON pd.product = p " +
            "          WHERE p.shop = :shop " +
            "             AND pd.price <> -1" +
            "             AND p.name = :name)" +
            "   ORDER BY pd.scrapDate DESC LIMIT 1")
    ProductBasedOnDateAttributes lastMaxPrice(String name, String shop);


    @Query(value = "SELECT DISTINCT avg(pd.price) FROM Product p " +
            " JOIN ProductBasedOnDateAttributes pd ON " +
            " pd.product = p WHERE p.shop = :shop " +
            " AND pd.price <> -1" +
            " AND p.name = :name")
    Double avgPrice(String name, String shop);
}
