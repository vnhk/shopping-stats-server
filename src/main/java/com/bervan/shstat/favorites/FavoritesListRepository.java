package com.bervan.shstat.favorites;

import com.bervan.history.model.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritesListRepository extends BaseRepository<FavoritesList, Long> {

    @Query(value =
            """
                    SELECT DISTINCT f FROM FavoriteProduct f
                    WHERE f.category = COALESCE(:category, f.category)
                        AND f.shop = COALESCE(:shop, f.shop)
                        AND f.listName = :favoritesListName
                        ORDER BY f.discountInPercent DESC
                    """
    )
    Page<FavoriteProduct> findFavorites(String favoritesListName, String shop, String category, Pageable pageable);
}
