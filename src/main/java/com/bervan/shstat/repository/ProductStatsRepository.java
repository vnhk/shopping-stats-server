package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductStats;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductStatsRepository extends BaseRepository<ProductStats, Long> {
    Optional<ProductStats> findByProductId(Long productId);

    @Query(nativeQuery = true, value = "SELECT COUNT(pda.id) FROM product_based_on_date_attributes pda" +
            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH) " +
            " AND (pda.deleted IS FALSE OR pda.deleted IS NULL) "
    )
    Long countAllPricesForXMonths(Integer monthOffset, @NotNull Long productId);

    @Query(nativeQuery = true, value = """
                SELECT AVG(pda.price)
                FROM product_based_on_date_attributes pda
                WHERE pda.product_id = :productId
                  AND pda.price > 0
                  AND pda.scrap_date >= DATE_SUB(CURRENT_DATE(), INTERVAL :monthOffset MONTH)
                  AND (pda.deleted IS FALSE OR pda.deleted IS NULL)
                  AND pda.scrap_date < (
                      SELECT MAX(p2.scrap_date)
                      FROM product_based_on_date_attributes p2
                      WHERE p2.product_id = :productId
                        AND p2.price > 0
                        AND (p2.deleted IS FALSE OR p2.deleted IS NULL)
                  )
            """)
    BigDecimal calculateAvgForXMonths(Integer monthOffset, @NotNull Long productId);

    @Query(nativeQuery = true, value = "SELECT MIN(pda.price) FROM product_based_on_date_attributes pda" +
            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_Date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH) " +
            " AND (pda.deleted IS FALSE OR pda.deleted IS NULL) "
    )
    BigDecimal findHistoricalLowForXMonths(Integer monthOffset, Long productId);
}
