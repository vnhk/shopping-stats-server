package com.shstat.summaryview;

import com.shstat.DataHolder;
import com.shstat.SearchService;
import com.shstat.ViewBuilder;
import com.shstat.dtomappers.BaseProductAttributesMapper;
import com.shstat.dtomappers.DTOMapper;
import com.shstat.dtomappers.ProductPriceStatsMapper;
import com.shstat.entity.Product;
import com.shstat.response.ProductDTO;
import com.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProductViewService extends ViewBuilder {
    private final SearchService searchService;

    public ProductViewService(SearchService searchService,
                              List<? extends DTOMapper<Product, ProductDTO>> productMappers) {
        super(productMappers, Set.of(BaseProductAttributesMapper.class, ProductPriceStatsMapper.class));
        this.searchService = searchService;
    }

    public SearchApiResponse findProductContainingName(String name) {
        List<Object> result = new ArrayList<>();
        List<Product> products = searchService.findProducts(name);
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();
            mappers.forEach(m -> m.map(DataHolder.of(product), DataHolder.of(productDTO)));
            result.add(productDTO);
        }

        return SearchApiResponse.builder().items(result).allFound((long) result.size()).build();
    }

    public SearchApiResponse findProductsByCategory(String category, String shop, Pageable pageable) {
        Page<Product> productsByCategory = searchService.findProductsByCategory(category, shop, pageable);
        SearchApiResponse response = SearchApiResponse.builder()
                .ofPage(productsByCategory)
                .build();

        List<ProductDTO> result = new ArrayList<>();
        for (Object item : response.getItems()) {
            ProductDTO productDTO = new ProductDTO();
            mappers.forEach(m -> m.map(DataHolder.of(item), DataHolder.of(productDTO)));
            result.add(productDTO);
        }

        response.setItems(result);
        return response;
    }
}
