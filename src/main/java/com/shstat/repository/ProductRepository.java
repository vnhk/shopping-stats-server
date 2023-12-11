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
                    ORDER BY id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeResInterface> historicalLowPriceProducts(Pageable pageable, String category, String shop);

    Product findProductByProductBasedOnDateAttributesId(Long id);

    @Query(nativeQuery = true, value =
            """
                    SELECT pda.id AS id, pda.scrap_date, pda.price
                    FROM scrapdb.product AS p
                    JOIN scrapdb.product_based_on_date_attributes AS pda ON p.id = pda.product_id
                    JOIN (
                        SELECT product_id, MAX(scrap_date) AS max_date
                        FROM scrapdb.product_based_on_date_attributes
                        WHERE price <> -1
                        GROUP BY product_id
                    ) AS latest_dates ON pda.product_id = latest_dates.product_id AND pda.scrap_date = latest_dates.max_date
                    WHERE (pda.product_id, pda.price) IN (
                        SELECT product_id, MIN(price) AS min_price
                        FROM scrapdb.product_based_on_date_attributes
                        WHERE price <> -1
                        GROUP BY product_id
                    ) AND (pda.product_id, pda.price) NOT IN (
                          SELECT product_id, MAX(price) AS max_price
                          FROM scrapdb.product_based_on_date_attributes
                          WHERE price <> -1
                          GROUP BY product_id
                    ) AND pda.price <= 0.80 * (
                          SELECT MIN(price)
                            FROM scrapdb.product_based_on_date_attributes
                          WHERE product_id = p.id
                            AND price NOT IN
                            (SELECT MIN(price)
                                FROM scrapdb.product_based_on_date_attributes
                             WHERE product_id = p.id
                                AND pda.price <> -1)
                            AND pda.price <> -1
                    )
                    ORDER BY p.id;
                    """
    )
        //not working very well
    Page<ProductBasedOnDateAttributesNativeResInterface> findXPercentLowerPriceThanHistoricalLow(Pageable pageable);

    @Query(value = "SELECT DISTINCT p.categories FROM Product p")
    Set<String> findCategories();

    @Modifying
    @Query(value = """
            CREATE OR REPLACE TABLE HISTORICAL_LOW_PRICES_TABLE AS
            SELECT pda.id AS id, pda.scrap_date, pda.price, p.shop, pc.categories
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
    @Transactional
    void refreshHistoricalLowPricesTable();

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
