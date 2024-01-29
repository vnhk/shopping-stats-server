package com.shstat.queue;

import com.shstat.favorites.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Slf4j
public class RefreshFavoritesViewsQueue extends AbstractQueue<RefreshFavoritesViewsQueueParam> {
    private final FavoriteService favoriteService;

    public RefreshFavoritesViewsQueue(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Override
    protected void process(Serializable object) {
        log.info("Refreshing favorites views started...");
        favoriteService.refreshTableForFavorites();
        log.info("Refreshing favorites views completed...");
    }
}
