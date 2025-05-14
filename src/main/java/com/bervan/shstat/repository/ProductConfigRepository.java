package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ProductConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Repository
public interface ProductConfigRepository extends BaseRepository<ProductConfig, Long> {
    @Query("SELECT DISTINCT c FROM ProductConfig p JOIN p.categories c WHERE (p.deleted IS FALSE OR p.deleted IS NULL)")
    Set<String> loadAllCategories();

    @Query("SELECT c FROM ProductConfig p JOIN p.categories c WHERE (p.deleted IS FALSE OR p.deleted IS NULL) AND p = :productConfig")
    List<String> loadAllCategories(ProductConfig productConfig);

    @Query("""
                SELECT p FROM ProductConfig p
                WHERE (p.deleted IS FALSE OR p.deleted IS NULL)
                  AND p.scrapTime <= :scrapTime
                  AND NOT EXISTS (
                      SELECT 1 FROM ScrapAudit sa
                      WHERE sa.productConfig = p
                        AND sa.date = :now
                  )
            """)
    Set<ProductConfig> findAllActiveForHour(@Param("scrapTime") LocalTime scrapTime, @Param("now") LocalDate now);
}
