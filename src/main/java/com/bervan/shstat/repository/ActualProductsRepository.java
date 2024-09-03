package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ActualProduct;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActualProductsRepository extends BaseRepository<ActualProduct, UUID> {
    Optional<ActualProduct> findByProductId(UUID productId);

    @Modifying
    @Query(value = """
            DELETE FROM scrapdb.actual_product
            WHERE scrap_date < DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL :offset DAY)
            OR scrap_date > CURRENT_TIMESTAMP()
                   """, nativeQuery = true)
    @Transactional
    void deleteNotActualProducts(Integer offset);

}
