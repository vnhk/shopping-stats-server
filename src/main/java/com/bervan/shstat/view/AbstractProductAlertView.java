package com.bervan.shstat.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchService;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.service.ProductAlertService;
import com.bervan.shstat.service.ProductConfigService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractProductAlertView extends AbstractBervanTableView<Long, ProductAlert> {
    public static final String ROUTE_NAME = "/shopping/product-alerts";
    private final ProductConfigService productConfigService;
    private final SearchService searchService;
    private Set<String> allAvailableCategories;

    public AbstractProductAlertView(ProductAlertService service, ProductConfigService productConfigService, SearchService searchService, BervanViewConfig bervanViewConfig) {
        super(new ShoppingLayout(ROUTE_NAME), service, bervanViewConfig, ProductAlert.class);
        this.productConfigService = productConfigService;
        this.searchService = searchService;
        renderCommonComponents();
        loadCategories();
        componentHelper = new ProductAlertsComponentHelper(new ArrayList<>(allAvailableCategories));
    }

    @Override
    protected void buildToolbarActionBar() {
        ProductAlertsToolbar toolbar = new ProductAlertsToolbar(checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, bervanViewConfig,
                (v) -> {
                    refreshData();
                    return v;
                }, service);

        // Pass floating toolbar for custom actions (if enabled)
        if (floatingToolbar != null) {
            toolbar.withFloatingToolbar(floatingToolbar);
        }

        tableToolbarActions = toolbar
                .withNotifyAboutProducts()
                .withDeleteButton()
                .withExportButton(isExportable(), service, pathToFileStorage, globalTmpDir)
                .build();
    }

    @Override
    protected ProductAlert preSaveActions(ProductAlert newItem) {
        return super.preSaveActions(newItem);
    }

    private void loadCategories() {
        allAvailableCategories = productConfigService.loadAllCategories();
    }

    @Override
    protected void postSaveActions(ProductAlert save) {
        super.postSaveActions(save);
        loadCategories();
    }

    @Override
    protected List<ProductAlert> loadData() {
        List<ProductAlert> productAlerts = super.loadData();

        //update not fetched categories
        for (ProductAlert productAlert : productAlerts) {
            productAlert.setProductCategories(loadCategories(productAlert));
        }

        //update not fetched emails
        for (ProductAlert productAlert : productAlerts) {
            productAlert.setEmails(loadEmails(productAlert));
        }

        return productAlerts;
    }

    private List<String> loadEmails(ProductAlert productAlert) {
        return ((ProductAlertService) service).loadAllEmails(productAlert);
    }

    private List<String> loadCategories(ProductAlert productAlert) {
        return ((ProductAlertService) service).loadAllCategories(productAlert);
    }

}
