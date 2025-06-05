package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductAlert;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAlertRepository extends BaseRepository<ProductAlert, Long> {
    List<ProductAlert> findAllByDeletedIsFalseOrDeletedNull();

    @Query("SELECT DISTINCT c FROM ProductAlert p JOIN p.productCategories c WHERE (p.deleted IS FALSE OR p.deleted IS NULL)")
    List<String> loadAllCategories(ProductAlert productAlert);

    @Query("SELECT DISTINCT c FROM ProductAlert p JOIN p.emails c WHERE (p.deleted IS FALSE OR p.deleted IS NULL)")
    List<String> loadAllEmails(ProductAlert productAlert);
}
