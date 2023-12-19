package com.shstat.favorites;

import com.shstat.DataHolder;
import com.shstat.ViewBuilder;
import com.shstat.dtomappers.DTOMapper;
import com.shstat.dtomappers.FavoritesBasicMapper;
import com.shstat.response.ProductDTO;
import com.shstat.response.SearchApiResponse;
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
                                List<? extends DTOMapper<FavoritesListRepository.ProductProjection, ProductDTO>> productMappers) {
        super(productMappers);
        this.favoriteService = favoriteService;
    }

    public SearchApiResponse favoriteService(Pageable pageable, String favoritesListName, String category, String shop) {
        Page<FavoritesListRepository.ProductProjection> favorites = favoriteService.getFavorites(pageable, favoritesListName, shop, category);
        return findProductGetResponse(favorites, pageable);
    }

    private SearchApiResponse findProductGetResponse(Page<FavoritesListRepository.ProductProjection> products, Pageable pageable) {
        List<Object> result = new ArrayList<>();
        for (FavoritesListRepository.ProductProjection product : products) {
            ProductDTO productDTO = new ProductDTO();
            mappersSubSet(Collections.singleton(FavoritesBasicMapper.class))
                    .forEach(m -> m.map(DataHolder.of(product), DataHolder.of(productDTO)));
            result.add(productDTO);
        }
        return SearchApiResponse.builder().ofPage(new PageImpl(result, pageable, products.getTotalElements()))
                .build();
    }
}
