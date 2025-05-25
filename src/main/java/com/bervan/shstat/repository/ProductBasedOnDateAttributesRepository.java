package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBasedOnDateAttributesRepository extends BaseRepository<ProductBasedOnDateAttributes, Long> {
    Boolean existsByProductIdAndFormattedScrapDate(Long productId, String formattedScrapDate);
}
