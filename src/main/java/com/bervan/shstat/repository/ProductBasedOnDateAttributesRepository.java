package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBasedOnDateAttributesRepository extends BaseRepository<ProductBasedOnDateAttributes, Long> {
    Boolean existsByProductIdAndFormattedScrapDate(Long productId, String formattedScrapDate);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM product_based_on_date_attributes_owners
                WHERE product_based_on_date_attributes_id = :itemId
            """, nativeQuery = true)
    void deleteOwners(Long itemId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM product_based_on_date_attributes
                WHERE id = :itemId
            """, nativeQuery = true)
    void deleteItem(Long itemId);

    List<ProductBasedOnDateAttributes> findAllByProductIdOrderByScrapDateDesc(Long productId);
}
