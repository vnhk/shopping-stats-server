package com.bervan.shstat.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.table.BervanFloatingToolbar;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.service.BaseService;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.service.ProductAlertService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ProductAlertsToolbar extends BervanTableToolbar<Long, ProductAlert> {
    private BervanButton notifyAboutProducts;
    private BervanFloatingToolbar floatingToolbar;

    public ProductAlertsToolbar(List<Checkbox> checkboxes, List<ProductAlert> data, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange, BervanViewConfig bervanViewConfig,
                                Function<Void, Void> refreshDataFunction,
                                BaseService<Long, ProductAlert> service) {
        super(checkboxes, data, ProductAlert.class, bervanViewConfig, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, refreshDataFunction, service);
    }

    /**
     * Sets the floating toolbar to add custom actions to.
     * Also enables icon buttons for modern UI consistency.
     */
    public ProductAlertsToolbar withFloatingToolbar(BervanFloatingToolbar floatingToolbar) {
        this.floatingToolbar = floatingToolbar;
        this.useIconButtons = true; // Enable icon buttons for consistency
        return this;
    }

    public ProductAlertsToolbar withNotifyAboutProducts() {
        notifyAboutProducts = new BervanButton(new Icon(VaadinIcon.ENVELOPE_O), ev -> {
            handleNotify();
        });
        notifyAboutProducts.getElement().setAttribute("title", "Force notification via email");
        notifyAboutProducts.addClassName("bervan-icon-btn");
        notifyAboutProducts.addClassName("info");

        actionsToBeAdded.add(notifyAboutProducts);

        // Add to floating toolbar if available
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                    "notify",
                    "vaadin:envelope-o",
                    "Force notification",
                    "info",
                    event -> handleNotify()
            );
        }

        return this;
    }

    private void handleNotify() {
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

            ((ProductAlertService) service).notifyAboutProducts(originals);

            checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
            selectAllCheckbox.setValue(false);
            showPrimaryNotification("Notifying ended!");
        });

        confirmDialog.setCancelText("Cancel");
        confirmDialog.setCancelable(true);
        confirmDialog.addCancelListener(event -> {
        });

        confirmDialog.open();
    }
}
