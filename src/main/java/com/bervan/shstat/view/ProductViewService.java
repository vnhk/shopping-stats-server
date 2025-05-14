package com.bervan.shstat.view;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.ViewBuilder;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductViewService extends ViewBuilder {
    private final ProductSearchService productSearchService;
    private Map<String, SearchApiResponse> lastFindProductsResponse = new HashMap<>();

    public ProductViewService(ProductSearchService productSearchService,
                              List<? extends DTOMapper<Product, ProductDTO>> productMappers) {
        super(productMappers);
        this.productSearchService = productSearchService;
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
        SearchApiResponse searchApiResponse = lastFindProductsResponse.get(category + shop + productName + pageable.getPageNumber() + pageable.getPageSize());
        if (searchApiResponse != null) {
            return searchApiResponse;
        }

        if (!lastFindProductsResponse.isEmpty()) {
            lastFindProductsResponse.clear();
        }

        Page<Product> productsByCategory = productSearchService.findProducts(category, shop, productName, pageable);
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
        lastFindProductsResponse.put(category + shop + productName + pageable.getPageNumber() + pageable.getPageSize(), response);
        return response;
    }
}
