package com.shstat.repository;

import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ProductBasedOnDateAttributesRepository extends JpaRepository<ProductBasedOnDateAttributes, Long> {
    Boolean existsByProductAndScrapDate(Product product, Date scrapDate);
}
