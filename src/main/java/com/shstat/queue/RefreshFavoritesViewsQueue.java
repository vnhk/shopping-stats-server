package com.shstat.queue;

import com.shstat.favorites.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class RefreshFavoritesViewsQueue extends AbstractQueue<RefreshFavoritesViewsQueueParam> {
    private final FavoriteService favoriteService;

    public RefreshFavoritesViewsQueue(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    public void run(Object object) {
        favoriteService.refreshTableForFavorites();
    }
}
