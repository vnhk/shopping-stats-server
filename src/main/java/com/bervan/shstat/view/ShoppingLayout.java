package com.bervan.shstat.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ShoppingLayout extends MenuNavigationComponent {

    public ShoppingLayout(String currentRoute) {
        super(currentRoute);

        addButtonIfVisible(menuButtonsRow, AbstractProductsView.ROUTE_NAME, "Search", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractBestOffersView.ROUTE_NAME, "Best Offers", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductView.ROUTE_NAME, "Product", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractShopConfigView.ROUTE_NAME, "Shop Config", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductConfigView.ROUTE_NAME, "Product Config", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractScrapAuditView.ROUTE_NAME, "Scrap Audit", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductAlertView.ROUTE_NAME, "Product Alerts", VaadinIcon.HOME.create());
        add(menuButtonsRow);
    }
}
