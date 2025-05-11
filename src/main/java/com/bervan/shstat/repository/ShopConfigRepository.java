package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ShopConfig;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopConfigRepository extends BaseRepository<ShopConfig, Long> {
}
