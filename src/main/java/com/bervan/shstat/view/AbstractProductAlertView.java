package com.bervan.shstat.view;

import com.bervan.common.AbstractBervanTableView;
import com.bervan.common.BervanButton;
import com.bervan.common.BervanButtonStyle;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.User;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.service.ProductAlertService;
import com.bervan.shstat.service.ProductConfigService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractProductAlertView extends AbstractBervanTableView<Long, ProductAlert> {
    public static final String ROUTE_NAME = "/shopping/product-alerts";
    private final ProductConfigService productConfigService;
    private final SearchService searchService;
    private Set<String> allAvailableCategories;
    private BervanButton notifyAboutProducts;

    public AbstractProductAlertView(ProductAlertService service, ProductConfigService productConfigService, SearchService searchService, BervanLogger log) {
        super(new ShoppingLayout(ROUTE_NAME), service, log, ProductAlert.class);
        this.productConfigService = productConfigService;
        this.searchService = searchService;
        renderCommonComponents();
        loadCategories();

        notifyAboutProducts = new BervanButton("Force notification via email", ev -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm");
            confirmDialog.setText("Are you sure you want to notify?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<ProductAlert> toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .toList();

                Set<ProductAlert> originals = new HashSet<>();
                for (ProductAlert alert : toSet) {
                    originals.add(service.loadById(alert.getId()).get());
                }

                service.notifyAboutProducts(originals);

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);
                showPrimaryNotification("Notifying ended!");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        buttonsForCheckboxesForVisibilityChange.add(notifyAboutProducts);
        for (Button button : buttonsForCheckboxesForVisibilityChange) {
            button.setEnabled(false);
        }

        checkboxActions.remove(checkboxDeleteButton);
        checkboxActions.add(notifyAboutProducts);
        checkboxActions.add(checkboxDeleteButton);
    }

    @Override
    protected List<String> getAllValuesForDynamicDropdowns(String key, ProductAlert item) {
        return new ArrayList<>();
    }

    @Override
    protected List<String> getAllValuesForDynamicMultiDropdowns(String key, ProductAlert item) {
        if (key.equals("productCategories")) {
            return allAvailableCategories.stream().sorted(String::compareTo).toList();
        }
        return new ArrayList<>();
    }

    private void loadCategories() {
        allAvailableCategories = productConfigService.loadAllCategories();
    }

    @Override
    protected List<String> getInitialSelectedValueForDynamicMultiDropdown(String key, ProductAlert item) {
        if (item != null && key.equals("productCategories")) {
            return item.getProductCategories();
        } else if (item != null && key.equals("emails")) {
            return item.getEmails();
        }
        return new ArrayList<>();
    }

    @Override
    protected String getInitialSelectedValueForDynamicDropdown(String key, ProductAlert item) {
        return null;
    }

    @Override
    protected ProductAlert customizeSavingInCreateForm(ProductAlert newItem) {
        ProductAlert productAlert = super.customizeSavingInCreateForm(newItem);
        productAlert.getOwners().add(loadCommonUser());
        return productAlert;
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
