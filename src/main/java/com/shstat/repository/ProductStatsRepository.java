package com.shstat.repository;

import com.shstat.entity.ProductStats;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductStatsRepository extends JpaRepository<ProductStats, Long> {
    Optional<ProductStats> findByProductId(Long productId);

    @Query(nativeQuery = true, value = "SELECT COUNT(pda.id) FROM scrapdb.product_based_on_date_attributes pda" +
            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH)")
    Long countAllPricesForXMonths(Integer monthOffset, @NotNull Long productId);

    @Query(nativeQuery = true, value = "SELECT AVG(pda.price) FROM scrapdb.product_based_on_date_attributes pda" +
            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH)")
    BigDecimal calculateAvgForXMonths(Integer monthOffset, @NotNull Long productId);

    @Query(nativeQuery = true, value = "SELECT MIN(pda.price) FROM scrapdb.product_based_on_date_attributes pda" +
            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_Date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH)")
    BigDecimal findHistoricalLowForXMonths(Integer monthOffset, Long productId);
}
