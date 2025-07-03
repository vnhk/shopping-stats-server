package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    Product findProductByProductBasedOnDateAttributesId(Long id);

    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE " +
            " c = COALESCE(:category, c) AND p.shop = COALESCE(:shop, p.shop) AND p.name LIKE %:productName%")
    Page<Product> findProductsBy(String category, String shop, String productName, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE p.shop = COALESCE(:shop, p.shop) " +
            "AND p.id IN :commonProductIds")
    Page<Product> findByShopAndIdsIn(Set<Long> commonProductIds,
                                     String shop,
                                     Pageable pageable);


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

}



