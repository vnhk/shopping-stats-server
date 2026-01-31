package com.bervan.shstat.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.table.BervanFloatingToolbar;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PricesListToolbar extends BervanTableToolbar<Long, ProductBasedOnDateAttributes> {
    private BervanButton decreasePrice10times;
    private BervanButton decreasePrice5times;
    private BervanButton decreasePrice2times;
    private Function<Void, Void> updateStatsOfProduct;
    private BervanFloatingToolbar floatingToolbar;

    public PricesListToolbar(List<Checkbox> checkboxes, List<ProductBasedOnDateAttributes> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange,
                             Function<Void, Void> updateStatsOfProduct, BervanViewConfig bervanViewConfig, Function<Void, Void> refreshDataFunction,
                             BaseService<Long, ProductBasedOnDateAttributes> service) {
        super(checkboxes, data, ProductBasedOnDateAttributes.class, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, refreshDataFunction, service);
        this.updateStatsOfProduct = updateStatsOfProduct;
    }

    /**
     * Sets the floating toolbar to add custom actions to.
     * Also enables icon buttons for modern UI consistency.
     */
    public PricesListToolbar withFloatingToolbar(BervanFloatingToolbar floatingToolbar) {
        this.floatingToolbar = floatingToolbar;
        this.useIconButtons = true; // Enable icon buttons for consistency
        return this;
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
                ProductBasedOnDateAttributes priceAttrInDB = service.loadById(priceAttr.getId()).get();
                priceAttrInDB.setPrice(newPrice);

                service.save(priceAttrInDB);
            }

            checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
            selectAllCheckbox.setValue(false);

            refreshDataFunction.apply(null);
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
        // Icon button with arrow-down and "10" label
        decreasePrice10times = new BervanButton(new Icon(VaadinIcon.ARROW_DOWN), setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(10);
            decreasePriceCommon(xTimes);
        });
        decreasePrice10times.getElement().setAttribute("title", "Divide price by 10");
        decreasePrice10times.addClassName("bervan-icon-btn");
        decreasePrice10times.addClassName("warning");
        decreasePrice10times.setText("÷10");

        actionsToBeAdded.add(decreasePrice10times);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "divide-10",
                    "vaadin:arrow-down",
                    "÷10",
                    "warning",
                    event -> {
                        BigDecimal xTimes = BigDecimal.valueOf(10);
                        decreasePriceCommon(xTimes);
                    }
            );
        }

        return this;
    }

    public PricesListToolbar withDecreasePrice5times() {
        decreasePrice5times = new BervanButton(new Icon(VaadinIcon.ARROW_DOWN), setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(5);
            decreasePriceCommon(xTimes);
        });
        decreasePrice5times.getElement().setAttribute("title", "Divide price by 5");
        decreasePrice5times.addClassName("bervan-icon-btn");
        decreasePrice5times.addClassName("warning");
        decreasePrice5times.setText("÷5");

        actionsToBeAdded.add(decreasePrice5times);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "divide-5",
                    "vaadin:arrow-down",
                    "÷5",
                    "warning",
                    event -> {
                        BigDecimal xTimes = BigDecimal.valueOf(5);
                        decreasePriceCommon(xTimes);
                    }
            );
        }

        return this;
    }

    public PricesListToolbar withDecreasePrice2times() {
        decreasePrice2times = new BervanButton(new Icon(VaadinIcon.ARROW_DOWN), setToLearnEvent -> {
            BigDecimal xTimes = BigDecimal.valueOf(2);
            decreasePriceCommon(xTimes);
        });
        decreasePrice2times.getElement().setAttribute("title", "Divide price by 2");
        decreasePrice2times.addClassName("bervan-icon-btn");
        decreasePrice2times.addClassName("warning");
        decreasePrice2times.setText("÷2");

        actionsToBeAdded.add(decreasePrice2times);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "divide-2",
                    "vaadin:arrow-down",
                    "÷2",
                    "warning",
                    event -> {
                        BigDecimal xTimes = BigDecimal.valueOf(2);
                        decreasePriceCommon(xTimes);
                    }
            );
        }

        return this;
    }
}
