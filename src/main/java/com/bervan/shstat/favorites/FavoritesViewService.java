package com.bervan.shstat.favorites;

import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.DataHolder;
import com.bervan.shstat.ViewBuilder;
import com.bervan.shstat.dtomappers.FavoritesBasicMapper;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FavoritesViewService extends ViewBuilder {
    private final FavoriteService favoriteService;

    public FavoritesViewService(FavoriteService favoriteService,
                                List<? extends DTOMapper<FavoriteProduct, ProductDTO>> productMappers) {
        super(productMappers);
        this.favoriteService = favoriteService;
    }

    public SearchApiResponse favoriteService(Pageable pageable, String favoritesListName, String category, String shop) {
        Page<FavoriteProduct> favorites = favoriteService.getFavorites(pageable, favoritesListName, shop, category);
        return findProductGetResponse(favorites, pageable);
    }

    private SearchApiResponse findProductGetResponse(Page<FavoriteProduct> products, Pageable pageable) {
        List<Object> result = new ArrayList<>();
        for (FavoriteProduct product : products) {
            ProductDTO productDTO = new ProductDTO();
            mappersSubSet(Collections.singleton(FavoritesBasicMapper.class))
                    .forEach(m -> m.map(DataHolder.of(product), DataHolder.of(productDTO)));
            result.add(productDTO);
        }
        return SearchApiResponse.builder().ofPage(new PageImpl(result, pageable, products.getTotalElements()))
                .build();
    }
}
