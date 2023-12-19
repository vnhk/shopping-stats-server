package com.shstat.favorites;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Repository
public interface FavoritesListRepository extends JpaRepository<FavoritesList, Long> {

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT product_id as id, shop, product_name as name, price, avg_price as avgPrice, discount_in_percent as discount,
                    scrap_date as scrapDate, img_src as imgSrc, offer_url as offerUrl
                    FROM scrapdb.FAVORITES_TABLE_VIEW
                    WHERE category = COALESCE(:category, category)
                        AND shop = COALESCE(:shop, shop)
                        AND list_name = :favoritesListName
                        ORDER BY discount_in_percent DESC;
                    """
    )
    Page<ProductProjection> findFavorites(String favoritesListName, String shop, String category, Pageable pageable);

    interface ProductProjection {
        Long getId();

        String getShop();

        String getName();

        String getOfferUrl();

        BigDecimal getPrice();

        BigDecimal getAvgPrice();

        Double getDiscount();

        Date getScrapDate();

        String getImgSrc();
    }
}
