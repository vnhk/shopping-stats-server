package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductStats;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStatsRepository extends BaseRepository<ProductStats, Long> {
    Optional<ProductStats> findByProductId(Long productId);

    List<ProductStats> findAllByProductIdIn(Collection<Long> productIds);

//    @Query(nativeQuery = true, value = """
//            SELECT
//                SUM(pda.price * DATEDIFF(
//                    LEAST(COALESCE(pda.scrap_date_end, CURRENT_DATE), CURRENT_DATE),
//                    pda.scrap_date
//                )) /\s
//                SUM(DATEDIFF(
//                    LEAST(COALESCE(pda.scrap_date_end, CURRENT_DATE), CURRENT_DATE),
//                    pda.scrap_date
//                )) AS weighted_avg_price
//            FROM product_based_on_date_attributes pda
//            WHERE pda.product_id = :productId
//              AND pda.price > 0
//              AND pda.scrap_date >= DATE_SUB(CURRENT_DATE(), INTERVAL :monthOffset MONTH)
//              AND pda.scrap_date < CURRENT_DATE
//              AND (pda.deleted IS FALSE OR pda.deleted IS NULL)
//            """)
//    BigDecimal calculateAvgForXMonths(Integer monthOffset, @NotNull Long productId);

//    @Query(nativeQuery = true, value = "SELECT MIN(pda.price) FROM product_based_on_date_attributes pda" +
//            " WHERE pda.product_id =:productId AND pda.price > 0 AND pda.scrap_Date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :monthOffset MONTH) " +
//            " AND (pda.deleted IS FALSE OR pda.deleted IS NULL) "
//    )
//    BigDecimal findHistoricalLowForXMonths(Integer monthOffset, Long productId);

    //better do it in java

}
