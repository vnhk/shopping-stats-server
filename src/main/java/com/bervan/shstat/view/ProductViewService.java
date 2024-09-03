package com.bervan.shstat.view;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.SearchService;
import com.bervan.shstat.ViewBuilder;
import com.bervan.shstat.dtomappers.BaseProductAttributesMapper;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.dtomappers.ProductPriceStatsMapper;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        super(productMappers);
        this.searchService = searchService;
    }

    public SearchApiResponse findById(Long id, Pageable pageable) {
        Page<Product> products = searchService.findById(id, pageable);
        return findProductGetResponse(products, pageable);
    }

    public SearchApiResponse findProductContainingName(String name, Pageable pageable) {
        Page<Product> products = searchService.findProducts(name, pageable);
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

    public SearchApiResponse findProductsByCategory(String category, String shop, Pageable pageable) {
        Page<Product> productsByCategory = searchService.findProductsByCategory(category, shop, pageable);
        SearchApiResponse response = SearchApiResponse.builder()
                .ofPage(productsByCategory)
                .build();

        List<ProductDTO> result = new ArrayList<>();
        for (Object item : response.getItems()) {
            ProductDTO productDTO = new ProductDTO();
            mappersSubSet(Set.of(BaseProductAttributesMapper.class, ProductPriceStatsMapper.class))
                    .forEach(m -> m.map(DataHolder.of(item), DataHolder.of(productDTO)));
            result.add(productDTO);
        }

        response.setItems(result);
        return response;
    }
}
