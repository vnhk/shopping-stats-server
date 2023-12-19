package com.shstat.favorites;

import com.shstat.DataHolder;
import com.shstat.ViewBuilder;
import com.shstat.dtomappers.DTOMapper;
import com.shstat.entity.Product;
import com.shstat.response.ProductDTO;
import com.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoritesViewService extends ViewBuilder {
    private final FavoriteService favoriteService;

    public FavoritesViewService(FavoriteService favoriteService,
                                List<? extends DTOMapper<Product, ProductDTO>> productMappers) {
        super(productMappers);
        this.favoriteService = favoriteService;
    }


    public SearchApiResponse favoriteService(Pageable pageable, String favoritesListName, String category, String shop) {
        Page<Product> products = favoriteService.getFavorites(pageable, favoritesListName, shop, category);
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
}
