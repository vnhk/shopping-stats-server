package com.shstat.shstat.repository;

import com.shstat.shstat.entity.Product;
import com.shstat.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndShopAndProductListNameAndProductListUrl(String name,
                                                                           String shop,
                                                                           String productListName,
                                                                           String productListUrl);

    List<Product> findByNameContainingAndShop(String name,
                                              String shop);

    @Query(value = "SELECT DISTINCT p.name FROM Product p WHERE p.shop = :shop")
    Set<String> findProductNames(String shop);

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
