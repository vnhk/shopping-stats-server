package com.shstat.repository;

import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Product> findByNameContaining(String name);

    @Query(value = "SELECT DISTINCT p.name FROM Product p WHERE p.shop = :shop")
    Set<String> findProductNames(String shop);

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
                      )
                    ORDER BY p.id;
                    """
    )
    Page<ProductBasedOnDateAttributesNativeRes> historicalLowPriceProducts(Pageable pageable);

    Product findProductByProductBasedOnDateAttributesId(Long id);

    interface ProductBasedOnDateAttributesNativeRes {
        Long getId();

        Date getScrapDate();

        BigDecimal getPrice();
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
