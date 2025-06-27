package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductBestOffer;
import com.bervan.shstat.entity.ProductStats;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductBestOfferRepository extends BaseRepository<ProductBestOffer, Long> {
}
