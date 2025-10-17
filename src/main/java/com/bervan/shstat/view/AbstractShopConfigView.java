package com.bervan.shstat.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.User;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.entity.scrap.ShopConfig;
import com.bervan.shstat.service.ShopConfigService;

public abstract class AbstractShopConfigView extends AbstractBervanTableView<Long, ShopConfig> {
    public static final String ROUTE_NAME = "/shopping/shop-config";
    private final BervanLogger log;
    private final SearchService searchService;

    public AbstractShopConfigView(ShopConfigService shopConfigService, SearchService searchService, BervanLogger log, BervanViewConfig bervanViewConfig) {
        super(new ShoppingLayout(ROUTE_NAME), shopConfigService, log, bervanViewConfig, ShopConfig.class);
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.searchService = searchService;
        this.log = log;

        renderCommonComponents();
    }

    private User loadCommonUser() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addDeletedFalseCriteria(User.class);
        searchRequest.addCriterion("U1", User.class, "username", SearchOperation.EQUALS_OPERATION, "COMMON_USER");
        SearchQueryOption options = new SearchQueryOption(User.class);

        return (User) searchService.search(searchRequest, options).getResultList().get(0);
    }

    @Override
    protected ShopConfig customizeSavingInCreateForm(ShopConfig newItem) {
        ShopConfig shopConfig = super.customizeSavingInCreateForm(newItem);
        shopConfig.getOwners().add(loadCommonUser());
        return shopConfig;
    }
}
