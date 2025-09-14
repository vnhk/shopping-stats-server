package com.bervan.shstat.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.service.GridActionService;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PricesListToolbar extends BervanTableToolbar<Long, ProductBasedOnDateAttributes> {
    private BervanButton decreasePrice10times;
    private BervanButton decreasePrice5times;
    private BervanButton decreasePrice2times;
    private Function<Void, Void> updateStatsOfProduct;

    public PricesListToolbar(GridActionService<Long, ProductBasedOnDateAttributes> gridActionService, List<Checkbox> checkboxes, List<ProductBasedOnDateAttributes> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange,
                             Function<Void, Void> updateStatsOfProduct) {
        super(gridActionService, checkboxes, data, ProductBasedOnDateAttributes.class, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange);
        this.updateStatsOfProduct = updateStatsOfProduct;
    }

    private void decreasePriceCommon(BigDecimal xTimes) {
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
                ProductBasedOnDateAttributes priceAttrInDB = gridActionService.service().loadById(priceAttr.getId()).get();
                priceAttrInDB.setPrice(newPrice);

                gridActionService.service().save(priceAttrInDB);
            }

            checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
            selectAllCheckbox.setValue(false);

            gridActionService.refreshData(data);
            showSuccessNotification("Changed state of " + toSet.size() + " items");

            updateStatsOfProduct.apply(null);
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }

    public PricesListToolbar withDecreasePrice10times() {
        decreasePrice10times = new BervanButton("-10x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(10);
            decreasePriceCommon(xTimes);
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(decreasePrice10times);
        return this;
    }

    public PricesListToolbar withDecreasePrice5times() {
        decreasePrice5times = new BervanButton("-5x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(5);
            decreasePriceCommon(xTimes);
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(decreasePrice5times);
        return this;
    }

    public PricesListToolbar withDecreasePrice2times() {
        decreasePrice2times = new BervanButton("-2x", setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(2);
            decreasePriceCommon(xTimes);
        }, BervanButtonStyle.WARNING);

        actionsToBeAdded.add(decreasePrice2times);
        return this;
    }
}