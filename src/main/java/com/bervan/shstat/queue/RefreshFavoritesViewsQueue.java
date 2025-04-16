package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.favorites.FavoriteService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class RefreshFavoritesViewsQueue extends AbstractQueue<RefreshFavoritesViewsQueueParam> {
    private final FavoriteService favoriteService;

    public RefreshFavoritesViewsQueue(BervanLogger log, FavoriteService favoriteService, ApiKeyService apiKeyService) {
        super(log, apiKeyService);
        this.favoriteService = favoriteService;
    }

    @Override
    protected void process(Serializable object) {
        log.info("Refreshing favorites views started...");
        favoriteService.refreshTableForFavorites();
        log.info("Refreshing favorites views completed...");
    }
}
