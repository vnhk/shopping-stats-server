package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ProductConfig;
import org.springframework.stereotype.Service;

@Service
public class ProductConfigService extends BaseService<Long, ProductConfig> {
    protected ProductConfigService(BaseRepository<ProductConfig, Long> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
