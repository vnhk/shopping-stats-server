package com.bervan.shstat.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.BervanComboBox;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.User;
import com.bervan.core.model.BervanLogger;
import com.bervan.history.model.Persistable;
import com.bervan.shstat.ProductConfigService;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ShopConfig;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProductConfigView extends AbstractTableView<Long, ProductConfig> {
    public static final String ROUTE_NAME = "/shopping/product-config";
    private final BervanLogger log;
    private final ComboBox<String> shopDropdown = new BervanComboBox<>();
    private final SearchService searchService;
    private Map<String, ShopConfig> shops;
    private String selectedShopName;

    public AbstractProductConfigView(ProductConfigService productConfigService, SearchService searchService, BervanLogger log) {
        super(new ShoppingLayout(ROUTE_NAME), productConfigService, log, ProductConfig.class);
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.searchService = searchService;
        this.log = log;

        loadShops();
        shopDropdown.setItems(shops.keySet());

        shopDropdown.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            selectedShopName = comboBoxStringComponentValueChangeEvent.getValue();
            this.loadData();
            this.refreshData();
        });

        renderCommonComponents();
        topLayout.add(shopDropdown);
    }

    private void loadShops() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addDeletedFalseCriteria(ShopConfig.class);
        SearchQueryOption options = new SearchQueryOption(ShopConfig.class);
        options.setSortField("shopName");
        shops = new HashMap<>();

        for (Persistable persistable : searchService.search(searchRequest, options).getResultList()) {
            ShopConfig shopConfig = (ShopConfig) persistable;
            shops.put(shopConfig.getShopName(), shopConfig);
        }

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
    protected ProductConfig customizeSavingInCreateForm(ProductConfig newItem) {
        ProductConfig productConfig = super.customizeSavingInCreateForm(newItem);
        productConfig.getOwners().add(loadCommonUser());
        return productConfig;
    }
}
