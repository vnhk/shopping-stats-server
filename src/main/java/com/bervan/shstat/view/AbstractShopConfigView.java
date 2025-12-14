package com.bervan.shstat.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.shstat.entity.scrap.ShopConfig;
import com.bervan.shstat.service.ShopConfigService;

public abstract class AbstractShopConfigView extends AbstractBervanTableView<Long, ShopConfig> {
    public static final String ROUTE_NAME = "/shopping/shop-config";

    public AbstractShopConfigView(ShopConfigService shopConfigService, BervanViewConfig bervanViewConfig) {
        super(new ShoppingLayout(ROUTE_NAME), shopConfigService, bervanViewConfig, ShopConfig.class);
        this.add(new ShoppingLayout(ROUTE_NAME));

        renderCommonComponents();
    }
}
