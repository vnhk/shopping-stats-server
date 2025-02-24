package com.bervan.shstat.favorites;

import com.bervan.shstat.response.ApiResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


//@RestController
//@RequestMapping(path = "/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final FavoritesViewService favoritesViewService;

    public FavoriteController(FavoriteService favoriteService, FavoritesViewService favoritesViewService) {
        this.favoriteService = favoriteService;
        this.favoritesViewService = favoritesViewService;
    }

    @GetMapping
    @CrossOrigin("*")
    public ResponseEntity<ApiResponse> getFavorites(Pageable pageable,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam String listName,
                                                    @RequestParam(required = false) String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }

        if (shop != null && StringUtils.isBlank(shop.trim())) {
            shop = null;
        }

        return ResponseEntity.ok(favoritesViewService.favoriteService(pageable, listName, category, shop));
    }

    @GetMapping("/lists")
    @CrossOrigin("*")
    public ResponseEntity<Set<String>> getFavoritesListsName() {
        return ResponseEntity.ok(favoriteService.getLists());
    }

}
