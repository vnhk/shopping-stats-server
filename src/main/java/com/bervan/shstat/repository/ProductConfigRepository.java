package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ProductConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductConfigRepository extends BaseRepository<ProductConfig, Long> {
    @Query("SELECT DISTINCT c FROM ProductConfig p JOIN p.categories c WHERE (p.deleted IS FALSE OR p.deleted IS NULL)")
    Set<String> loadAllCategories();

    @Query("SELECT c FROM ProductConfig p JOIN p.categories c WHERE (p.deleted IS FALSE OR p.deleted IS NULL) AND p = :productConfig")
    List<String> loadAllCategories(ProductConfig productConfig);
}
