package com.bervan.shstat.view;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.ViewBuilder;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductViewService extends ViewBuilder {
    private final ProductSearchService productSearchService;
    private final Map<SearchQueryKey, SearchApiResponse> cache =
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<SearchQueryKey, SearchApiResponse> eldest) {
                    return this.size() > 5;
                }
            };

    public ProductViewService(ProductSearchService productSearchService,
                              List<? extends DTOMapper<Product, ProductDTO>> productMappers) {
        super(productMappers);
        this.productSearchService = productSearchService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void clearCache() {
        cache.clear();
    }

    public SearchApiResponse findById(Long id, Pageable pageable) {
        Page<Product> products = productSearchService.findById(id, pageable);
        return findProductGetResponse(products, pageable);
    }

    public SearchApiResponse findProductContainingName(String name, Pageable pageable) {
        Page<Product> products = productSearchService.findProducts(name, pageable);
        return findProductGetResponse(products, pageable);
    }

    private SearchApiResponse findProductGetResponse(Page<Product> products, Pageable pageable) {
        List<Object> result = new ArrayList<>();
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();
            mappersSubSet(PRODUCT_WITH_DETAILS_AND_PRICE_HISTORY_MAPPERS)
                    .forEach(m -> m.map(DataHolder.of(product), DataHolder.of(productDTO)));
            result.add(productDTO);
        }
        return SearchApiResponse.builder().ofPage(new PageImpl(result, pageable, products.getTotalElements()))
                .build();
    }

    public SearchApiResponse findProducts(String category, String shop, String productName, Pageable pageable) {
        SearchQueryKey key = new SearchQueryKey(category, shop, productName, pageable.getPageNumber(), pageable.getPageSize());

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Page<Product> productsByCategory = productSearchService.findProductsByTokens(category, shop, productName, pageable);
        SearchApiResponse response = SearchApiResponse.builder()
                .ofPage(productsByCategory)
                .build();

        List<ProductDTO> result = new ArrayList<>();
        for (Object item : response.getItems()) {
            ProductDTO productDTO = new ProductDTO();
            mappersSubSet(PRODUCT_WITH_DETAILS_AND_PRICE_HISTORY_MAPPERS)
                    .forEach(m -> m.map(DataHolder.of(item), DataHolder.of(productDTO)));
            result.add(productDTO);
        }

        response.setItems(result);
        cache.put(key, response);
        return response;
    }

    private static class SearchQueryKey {
        private final String category;
        private final String shop;
        private final String productName;
        private final int page;
        private final int size;

        public SearchQueryKey(String category, String shop, String productName, int page, int size) {
            this.category = Objects.toString(category, "");
            this.shop = Objects.toString(shop, "");
            this.productName = Objects.toString(productName, "");
            this.page = page;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SearchQueryKey)) return false;
            SearchQueryKey that = (SearchQueryKey) o;
            return page == that.page &&
                    size == that.size &&
                    category.equals(that.category) &&
                    shop.equals(that.shop) &&
                    productName.equals(that.productName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(category, shop, productName, page, size);
        }
    }
}
