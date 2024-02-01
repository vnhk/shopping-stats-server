package com.shstat.repository;

import com.shstat.entity.ActualProduct;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActualProductsRepository extends JpaRepository<ActualProduct, Long> {
    Optional<ActualProduct> findByProductId(Long productId);

    @Modifying
    @Query(value = """
            DELETE FROM scrapdb.ACTUAL_PRODUCTS
            WHERE scrap_date < DATE_SUB(CURDATE(), INTERVAL :offset DAY)
            OR scrap_date > CURDATE()
                   """, nativeQuery = true)
    @Transactional
    void deleteNotActualProducts(Integer offset);

}
