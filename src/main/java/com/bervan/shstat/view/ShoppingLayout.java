package com.bervan.shstat.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ShoppingLayout extends MenuNavigationComponent {

    public ShoppingLayout(String currentRoute) {
        super(currentRoute);

        addButtonIfVisible(menuButtonsRow, AbstractProductsView.ROUTE_NAME, "Search", VaadinIcon.SEARCH.create());
        addButtonIfVisible(menuButtonsRow, AbstractBestOffersView.ROUTE_NAME, "Best Offers", VaadinIcon.THUMBS_UP.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductView.ROUTE_NAME, "Product", VaadinIcon.PACKAGE.create());
        addButtonIfVisible(menuButtonsRow, AbstractShopConfigView.ROUTE_NAME, "Shop Config", VaadinIcon.COG.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductConfigView.ROUTE_NAME, "Product Config", VaadinIcon.TOOLS.create());
        addButtonIfVisible(menuButtonsRow, AbstractScrapAuditView.ROUTE_NAME, "Scrap Audit", VaadinIcon.CLIPBOARD_TEXT.create());
        addButtonIfVisible(menuButtonsRow, AbstractProductAlertView.ROUTE_NAME, "Product Alerts", VaadinIcon.BELL.create());
        add(menuButtonsRow);
    }
}
