package com.bervan.shstat.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.repository.ProductBasedOnDateAttributesRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductBasedOnDateAttributesService extends BaseService<Long, ProductBasedOnDateAttributes> {
    protected ProductBasedOnDateAttributesService(ProductBasedOnDateAttributesRepository repository, SearchService searchService) {
        super(repository, searchService);
    }

    public static void moveScrapDates(List<ProductBasedOnDateAttributes> productBasedOnDateAttributes) {
        List<ProductBasedOnDateAttributes> sortedPrices = productBasedOnDateAttributes.stream()
                .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                .toList();

        for (int i = 1; i < sortedPrices.size(); i++) {
            ProductBasedOnDateAttributes current = sortedPrices.get(i);
            ProductBasedOnDateAttributes previous = sortedPrices.get(i - 1);

            current.setScrapDateEnd(previous.getScrapDate());
        }

        if (!sortedPrices.isEmpty()) {
            sortedPrices.get(0).setScrapDateEnd(null);
        }
    }

    @Override
    public Optional<ProductBasedOnDateAttributes> loadById(Long aLong) {
        return repository.findById(aLong);
    }

    @Override
    public void delete(ProductBasedOnDateAttributes item) {
        ((ProductBasedOnDateAttributesRepository) repository).deleteOwners(item.getId());
        ((ProductBasedOnDateAttributesRepository) repository).deleteItem(item.getId());

        //update scrapStart scrapEnd in existing
        List<ProductBasedOnDateAttributes> productBasedOnDateAttributes = item.getProduct().getProductBasedOnDateAttributes();
        boolean empty = productBasedOnDateAttributes.isEmpty();
        if (empty) {
            productBasedOnDateAttributes = ((ProductBasedOnDateAttributesRepository) repository).findAllByProductIdOrderByScrapDateDesc(item.getProduct().getId());
        }

        moveScrapDates(productBasedOnDateAttributes);
    }

    public boolean existsByProductIdAndFormattedScrapDate(Long id, String formattedScrapDate) {
        return ((ProductBasedOnDateAttributesRepository) repository).existsByProductIdAndFormattedScrapDate(id, formattedScrapDate);
    }
}
