package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ProductBasedOnDateAttributesRepository extends BaseRepository<ProductBasedOnDateAttributes, Long> {
    Boolean existsByProductAndScrapDate(Product product, Date scrapDate);
}
