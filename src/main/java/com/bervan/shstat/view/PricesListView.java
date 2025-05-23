package com.bervan.shstat.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.BervanButton;
import com.bervan.common.BervanButtonStyle;
import com.bervan.common.BervanLoggerImpl;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.BaseService;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.ProductService;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
public class PricesListView extends AbstractTableView<Long, ProductBasedOnDateAttributes> {
    private final Product product;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final AbstractProductView parentView;
    private BervanButton decreasePrice10times;
    private BervanButton decreasePrice5times;
    private BervanButton decreasePrice2times;

    public PricesListView(AbstractProductView parentView, BaseService<Long, ProductBasedOnDateAttributes> service, ProductService productService, ShoppingLayout pageLayout, Product product, UserRepository userRepository) {
        super(pageLayout, service, BervanLoggerImpl.init(log), ProductBasedOnDateAttributes.class);
        this.product = product;
        this.productService = productService;
        this.parentView = parentView;
        this.userRepository = userRepository;
        renderCommonComponents();

        decreasePrice10times = new BervanButton("-10x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(10);
            decreasePriceCommon(service, xTimes);
        }, BervanButtonStyle.WARNING);

        decreasePrice5times = new BervanButton("-5x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(5);
            decreasePriceCommon(service, xTimes);
        }, BervanButtonStyle.WARNING);

        decreasePrice2times = new BervanButton("-2x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(2);
            decreasePriceCommon(service, xTimes);
        }, BervanButtonStyle.WARNING);


        buttonsForCheckboxesForVisibilityChange.add(decreasePrice2times);
        buttonsForCheckboxesForVisibilityChange.add(decreasePrice5times);
        buttonsForCheckboxesForVisibilityChange.add(decreasePrice10times);
        for (Button button : buttonsForCheckboxesForVisibilityChange) {
            button.setEnabled(false);
        }

        checkboxActions.remove(checkboxDeleteButton);
        checkboxActions.add(decreasePrice2times);
        checkboxActions.add(decreasePrice5times);
        checkboxActions.add(decreasePrice10times);
        checkboxActions.add(checkboxDeleteButton);
    }

    private void decreasePriceCommon(BaseService<Long, ProductBasedOnDateAttributes> service, BigDecimal xTimes) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirm decreasing prices " + xTimes + " times");
        confirmDialog.setText("Are you sure you want to decrease price(s) " + xTimes + " times?");

        confirmDialog.setConfirmText("Yes");
        confirmDialog.setConfirmButtonTheme("primary");
        confirmDialog.addConfirmListener(event -> {
            Set<String> itemsId = getSelectedItemsByCheckbox();

            List<ProductBasedOnDateAttributes> toSet = data.stream()
                    .filter(e -> e.getId() != null)
                    .filter(e -> itemsId.contains(e.getId().toString()))
                    .toList();

            for (ProductBasedOnDateAttributes priceAttr : toSet) {
                BigDecimal newPrice = priceAttr.getPrice().divide(xTimes);
                priceAttr.setPrice(newPrice);
                ProductBasedOnDateAttributes priceAttrInDB = service.loadById(priceAttr.getId()).get();
                priceAttrInDB.setPrice(newPrice);

                service.save(priceAttrInDB);
            }

            checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
            selectAllCheckbox.setValue(false);

            refreshData();
            showSuccessNotification("Changed state of " + toSet.size() + " items");

            updateStatsOfProduct();
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }

    @Override
    protected Grid<ProductBasedOnDateAttributes> getGrid() {
        Grid<ProductBasedOnDateAttributes> grid = new Grid<>(ProductBasedOnDateAttributes.class, false);
        buildGridAutomatically(grid);

        return grid;
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.addCriterion("PRODUCT_TASK_CRITERIA", ProductBasedOnDateAttributes.class,
                "product.id", SearchOperation.EQUALS_OPERATION, product.getId());
        request.setAddOwnerCriterion(false);
        super.customizePreLoad(request);
    }

    @Override
    protected ProductBasedOnDateAttributes customizeSavingInCreateForm(ProductBasedOnDateAttributes newItem) {
        newItem.setProduct(product);
        newItem.addOwner(userRepository.findByUsername("COMMON_USER").get());
        return super.customizeSavingInCreateForm(newItem);
    }

    @Override
    protected void postSaveActions() {
        super.postSaveActions();
    }

    @Override
    protected void customPostUpdate(ProductBasedOnDateAttributes changed) {
        updateStatsOfProduct();
    }

    private void updateStatsOfProduct() {
        productService.updateStats(product);
        parentView.reload();
    }
}