package com.bervan.shstat.favorites;

import com.bervan.history.model.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteProductsRepository extends BaseRepository<FavoriteProduct, Long> {


}
