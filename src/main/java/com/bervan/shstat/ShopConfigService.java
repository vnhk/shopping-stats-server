package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ShopConfig;
import org.springframework.stereotype.Service;

@Service
public class ShopConfigService extends BaseService<Long, ShopConfig> {
    protected ShopConfigService(BaseRepository<ShopConfig, Long> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
