package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductBasedOnDateAttributesService extends BaseService<Long, ProductBasedOnDateAttributes> {
    protected ProductBasedOnDateAttributesService(BaseRepository<ProductBasedOnDateAttributes, Long> repository, SearchService searchService) {
        super(repository, searchService);
    }

    @Override
    public Optional<ProductBasedOnDateAttributes> loadById(Long aLong) {
        return repository.findById(aLong);
    }
}
