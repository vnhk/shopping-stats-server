package com.bervan.shstat;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.favorites.FavoriteController;

//@Service
public class ShopSchedulerTasks {
    private final FavoriteController favoriteController;
    private final ProductController productController;
    private final BervanLogger log;

    public ShopSchedulerTasks(FavoriteController favoriteController, ProductController productController, BervanLogger log) {
        this.favoriteController = favoriteController;
        this.productController = productController;
        this.log = log;
    }

//    @Scheduled(cron = "0 0 3 * * *")
    public void refreshView() {
        try {
            productController.refreshMaterializedViews();
        } catch (Exception e) {
            log.error("RefreshingViews: FAILED!", e);
        }
    }

//    @Scheduled(cron = "0 0 * * * *")
    public void refreshFavorites() throws InterruptedException {
        Thread.sleep(15000 + RandomUtil.getPositiveInt() % 15000);
        try {
            favoriteController.refreshMaterializedViews();
        } catch (Exception e) {
            log.error("RefreshingViews: FAILED!", e);
        }
    }
}
