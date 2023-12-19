package com.shstat.favorites;

import com.shstat.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritesListRepository extends JpaRepository<FavoritesList, Long> {

    @Query(nativeQuery = true, value =
            """
                    SELECT DISTINCT p.id FROM scrapdb.product p join scrapdb.FAVORITES_TABLE_VIEW FTV on p.id = FTV.product_id
                    WHERE FTV.category = COALESCE(:category, FTV.category)
                        AND FTV.shop = COALESCE(:shop, FTV.shop)
                        AND FTV.list_name = :favoritesListName
                        ORDER BY id;
                    """
    )
    Page<ProductProjection> findFavorites(String favoritesListName, String shop, String category, Pageable pageable);

    interface ProductProjection {
        Long getId();
    }
}
