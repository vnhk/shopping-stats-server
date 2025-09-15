package com.bervan.shstat.view;

import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.User;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.service.ProductAlertService;
import com.bervan.shstat.service.ProductConfigService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class AbstractProductAlertView extends AbstractBervanTableView<Long, ProductAlert> {
    public static final String ROUTE_NAME = "/shopping/product-alerts";
    private final ProductConfigService productConfigService;
    private final SearchService searchService;
    private Set<String> allAvailableCategories;

    public AbstractProductAlertView(ProductAlertService service, ProductConfigService productConfigService, SearchService searchService, BervanLogger log) {
        super(new ShoppingLayout(ROUTE_NAME), service, log, ProductAlert.class);
        this.productConfigService = productConfigService;
        this.searchService = searchService;
        renderCommonComponents();
        loadCategories();
        componentHelper = new ProductAlertsComponentHelper(new ArrayList<>(allAvailableCategories));
    }

    @Override
    protected void buildToolbarActionBar() {
        tableToolbarActions = new ProductAlertsToolbar(gridActionService, checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange)
                .withNotifyAboutProducts()
                .withDeleteButton()
                .withExportButton(isExportable(), service, bervanLogger, pathToFileStorage, globalTmpDir)
                .build();
    }

    @Override
    protected ProductAlert customizeSavingInCreateForm(ProductAlert newItem) {
        ProductAlert productAlert = super.customizeSavingInCreateForm(newItem);
        productAlert.getOwners().add(loadCommonUser());
        return productAlert;
    }

    private void loadCategories() {
        allAvailableCategories = productConfigService.loadAllCategories();
    }

    @Override
    protected void postSaveActions() {
        super.postSaveActions();
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

    private User loadCommonUser() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setAddOwnerCriterion(false);
        searchRequest.addDeletedFalseCriteria(User.class);
        searchRequest.addCriterion("U1", User.class, "username", SearchOperation.EQUALS_OPERATION, "COMMON_USER");
        SearchQueryOption options = new SearchQueryOption(User.class);

        return (User) searchService.search(searchRequest, options).getResultList().get(0);
    }

}
