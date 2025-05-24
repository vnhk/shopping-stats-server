package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ScrapAuditRepository extends BaseRepository<ScrapAudit, Long> {


    @Query("""
            SELECT s FROM ScrapAudit s JOIN ProductConfig pc ON s.productConfig = pc
                JOIN ShopConfig sc ON pc.shop = sc
                     WHERE pc.name = :productListName AND pc.url = :productListUrl
                     AND sc.shopName = :shop
                     AND s.date = :now
            """)
    Optional<ScrapAudit> findByProductConfigAndDate(String shop, String productListName, String productListUrl, LocalDate now);
}
