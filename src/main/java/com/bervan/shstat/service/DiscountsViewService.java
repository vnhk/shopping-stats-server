package com.bervan.shstat.service;

import com.bervan.logging.JsonLogger;
import com.bervan.shstat.DataHolder;
import com.bervan.shstat.ViewBuilder;
import com.bervan.shstat.dtomappers.BaseProductAttributesMapper;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.dtomappers.ProductPricesMapper;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.view.ProductViewService;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscountsViewService extends ViewBuilder {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");
    private final ProductSearchService productSearchService;
    private final ProductViewService productViewService;
    private final Map<DiscountQueryKey, SearchApiResponse> cache =
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<DiscountQueryKey, SearchApiResponse> eldest) {
                    return this.size() > 5;
                }
            };

    public DiscountsViewService(ProductSearchService productSearchService,
                                List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                                List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice, ProductViewService productViewService) {
        super(getSet(productMappers, productBasedOnDateAttributesToPrice));
        this.productSearchService = productSearchService;
        this.productViewService = productViewService;
    }

    private static Set getSet(List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                              List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice) {
        Set all = new HashSet<>(productMappers);
        all.addAll(productBasedOnDateAttributesToPrice);
        return all;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void clearCacheAtMidnight() {
        cache.clear();
    }

    public SearchApiResponse findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, List<String> categories, String shop, String name, Integer prevPriceMin, Integer prevPriceMax) {
        DiscountQueryKey key = new DiscountQueryKey(pageable.getPageNumber(), pageable.getPageSize(),
                discountMin, discountMax, months, categories, shop, name, prevPriceMin, prevPriceMax);
        log.debug("findDiscountsComparedToAVGOnPricesInLastXMonths: {}", key);

        if (cache.containsKey(key)) {
            SearchApiResponse searchApiResponse = cache.get(key);
            log.debug("findDiscountsComparedToAVGOnPricesInLastXMonths: {}\n from cache: {} items", key, searchApiResponse.getAllFound());
            return searchApiResponse;
        }

        Page<Product> queryResult =
                productSearchService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin,
                        discountMax, months, categories, shop, name, prevPriceMin, prevPriceMax);

        SearchApiResponse response = buildResponse(pageable, queryResult);
        cache.put(key, response);
        productViewService.updateCacheWithProductsById(queryResult);
        log.debug("findDiscountsComparedToAVGOnPricesInLastXMonths: {}\n from db: {} items", key, response.getAllFound());
        return response;
    }

    private SearchApiResponse buildResponse(Pageable pageable, Page<Product> queryResult) {
        Collection<Object> result = new ArrayList<>();

        for (Product product : queryResult) {
            ProductDTO productDTO = new ProductDTO();
            mappersMap.get(BaseProductAttributesMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
            mappersMap.get(ProductPricesMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
            result.add(productDTO);
        }

        return SearchApiResponse.builder()
                .items(result)
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .allFound(queryResult.getTotalElements())
                .allPages(queryResult.getTotalPages())
                .build();
    }

    @ToString
    private static class DiscountQueryKey {
        private final int page;
        private final int size;
        private final Double discountMin;
        private final Double discountMax;
        private final Integer months;
        private final List<String> categories;
        private final String shop;
        private final String name;
        private final Integer prevPriceMin;
        private final Integer prevPriceMax;

        public DiscountQueryKey(int page, int size,
                                Double discountMin, Double discountMax, Integer months,
                                List<String> categories, String shop, String name,
                                Integer prevPriceMin, Integer prevPriceMax) {
            this.page = page;
            this.size = size;
            this.discountMin = discountMin;
            this.discountMax = discountMax;
            this.months = months;
            this.categories = categories;
            this.shop = Objects.toString(shop, "");
            this.name = Objects.toString(name, "");
            this.prevPriceMin = prevPriceMin;
            this.prevPriceMax = prevPriceMax;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscountQueryKey)) return false;
            DiscountQueryKey that = (DiscountQueryKey) o;
            return page == that.page &&
                    size == that.size &&
                    Objects.equals(discountMin, that.discountMin) &&
                    Objects.equals(discountMax, that.discountMax) &&
                    Objects.equals(months, that.months) &&
                    categories.equals(that.categories) &&
                    shop.equals(that.shop) &&
                    name.equals(that.name) &&
                    Objects.equals(prevPriceMin, that.prevPriceMin) &&
                    Objects.equals(prevPriceMax, that.prevPriceMax);
        }

        @Override
        public int hashCode() {
            return Objects.hash(page, size, discountMin, discountMax, months,
                    categories, shop, name, prevPriceMin, prevPriceMax);
        }
    }
}
