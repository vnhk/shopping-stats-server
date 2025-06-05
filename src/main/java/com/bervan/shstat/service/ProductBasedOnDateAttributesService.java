package com.bervan.shstat.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.repository.ProductBasedOnDateAttributesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductBasedOnDateAttributesService extends BaseService<Long, ProductBasedOnDateAttributes> {
    protected ProductBasedOnDateAttributesService(ProductBasedOnDateAttributesRepository repository, SearchService searchService) {
        super(repository, searchService);
    }

    @Override
    public Optional<ProductBasedOnDateAttributes> loadById(Long aLong) {
        return repository.findById(aLong);
    }

    @Override
    public void delete(ProductBasedOnDateAttributes item) {
        ((ProductBasedOnDateAttributesRepository) repository).deleteOwners(item.getId());
        ((ProductBasedOnDateAttributesRepository) repository).deleteItem(item.getId());
    }
}
