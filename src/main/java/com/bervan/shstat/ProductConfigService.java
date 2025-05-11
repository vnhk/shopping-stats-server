package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.repository.ProductConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductConfigService extends BaseService<Long, ProductConfig> {
    protected ProductConfigService(BaseRepository<ProductConfig, Long> repository, SearchService searchService) {
        super(repository, searchService);
    }

    public Set<String> loadAllCategories() {
        return ((ProductConfigRepository) repository).loadAllCategories();
    }

    public List<String> loadAllCategories(ProductConfig productConfig) {
        return ((ProductConfigRepository) repository).loadAllCategories(productConfig);
    }
}
