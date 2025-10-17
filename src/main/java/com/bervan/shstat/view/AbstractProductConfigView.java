package com.bervan.shstat.view;

import com.bervan.common.component.BervanComboBox;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.Operator;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.User;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.core.model.BervanLogger;
import com.bervan.history.model.Persistable;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ShopConfig;
import com.bervan.shstat.service.ProductConfigService;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.*;

public abstract class AbstractProductConfigView extends AbstractBervanTableView<Long, ProductConfig> {
    public static final String ROUTE_NAME = "/shopping/product-config";
    private final BervanLogger log;
    private final ComboBox<String> shopDropdown = new BervanComboBox<>();
    private final SearchService searchService;
    private Map<String, ShopConfig> shops;
    private Set<String> allAvailableCategories;
    private String selectedShopName;

    public AbstractProductConfigView(ProductConfigService productConfigService, SearchService searchService, BervanLogger log, BervanViewConfig bervanViewConfig) {
        super(new ShoppingLayout(ROUTE_NAME), productConfigService, log, bervanViewConfig, ProductConfig .class);
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.searchService = searchService;
        this.log = log;
        loadCategories();

        loadShops();
        shopDropdown.setItems(shops.keySet());

        shopDropdown.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            selectedShopName = comboBoxStringComponentValueChangeEvent.getValue();
            this.loadData();
            this.refreshData();
        });

        renderCommonComponents();
        topLayout.add(shopDropdown);

        this.componentHelper = new ProductConfigComponentHelper(shops, new ArrayList<>(allAvailableCategories), this::loadCategories);
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

    private void loadCategories() {
        allAvailableCategories = ((ProductConfigService) service).loadAllCategories();
    }

    private List<String> loadCategories(ProductConfig productConfig) {
        return ((ProductConfigService) service).loadAllCategories(productConfig);
    }

    @Override
    protected ProductConfig customizeSavingInCreateForm(ProductConfig newItem) {
        ProductConfig productConfig = super.customizeSavingInCreateForm(newItem);
        productConfig.getOwners().add(loadCommonUser());
        return productConfig;
    }

    @Override
    protected void postSaveActions() {
        super.postSaveActions();
        loadCategories();
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.addCriterion("SHOP_NAME_EQ_CRITERION", Operator.OR_OPERATOR, ProductConfig.class,
                "shop.shopName", SearchOperation.EQUALS_OPERATION, selectedShopName);
        request.setAddOwnerCriterion(false);
    }

    @Override
    protected List<ProductConfig> loadData() {
        List<ProductConfig> productConfigs = super.loadData();

        //update not fetched categories
        for (ProductConfig productConfig : productConfigs) {
            productConfig.setCategories(loadCategories(productConfig));
        }
        return productConfigs;
    }

    private User loadCommonUser() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addDeletedFalseCriteria(User.class);
        searchRequest.addCriterion("U1", User.class, "username", SearchOperation.EQUALS_OPERATION, "COMMON_USER");
        SearchQueryOption options = new SearchQueryOption(User.class);

        return (User) searchService.search(searchRequest, options).getResultList().get(0);
    }
}
