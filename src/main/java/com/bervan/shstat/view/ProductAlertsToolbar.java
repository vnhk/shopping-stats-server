package com.bervan.shstat.view;

import com.bervan.common.BervanTableToolbar;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.service.GridActionService;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.service.ProductAlertService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAlertsToolbar extends BervanTableToolbar<Long, ProductAlert> {
    private BervanButton notifyAboutProducts;

    public ProductAlertsToolbar(GridActionService<Long, ProductAlert> gridActionService, List<Checkbox> checkboxes, List<ProductAlert> data, Class<?> tClass, Checkbox selectAllCheckbox, List<Button> buttonsForCheckboxesForVisibilityChange) {
        super(gridActionService, checkboxes, data, tClass, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange);
    }

    public ProductAlertsToolbar withNotifyAboutProducts() {
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
                    originals.add(gridActionService.service().loadById(alert.getId()).get());
                }

                ((ProductAlertService) gridActionService.service()).notifyAboutProducts(originals);

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

        actionsToBeAdded.add(notifyAboutProducts);
        return this;
    }
}