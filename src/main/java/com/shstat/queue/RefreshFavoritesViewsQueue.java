package com.shstat.queue;

import com.shstat.favorites.FavoriteService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class RefreshFavoritesViewsQueue extends AbstractQueue<RefreshFavoritesViewsQueueParam> {
    private final FavoriteService favoriteService;

    public RefreshFavoritesViewsQueue(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    public void run(Serializable object) {
        favoriteService.refreshTableForFavorites();
    }
}
