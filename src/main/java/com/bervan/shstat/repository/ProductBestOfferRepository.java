package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductBestOffer;
import com.bervan.shstat.entity.ProductStats;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBestOfferRepository extends BaseRepository<ProductBestOffer, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM product_best_offer_owners WHERE 1=1
            """, nativeQuery = true)
    void deleteAllOwners();

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM product_best_offer WHERE 1=1
            """, nativeQuery = true)
    void deleteAllItems();
}
