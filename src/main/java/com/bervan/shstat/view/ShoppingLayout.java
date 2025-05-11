package com.bervan.shstat.view;

import com.bervan.common.MenuNavigationComponent;

public class ShoppingLayout extends MenuNavigationComponent {

    public ShoppingLayout(String currentRoute) {
        super(currentRoute);

        addButtonIfVisible(menuButtonsRow, AbstractProductsView.ROUTE_NAME, "Search");
        addButtonIfVisible(menuButtonsRow, AbstractBestOffersView.ROUTE_NAME, "Best Offers");
        addButtonIfVisible(menuButtonsRow, AbstractProductView.ROUTE_NAME, "Product");
        addButtonIfVisible(menuButtonsRow, AbstractShopConfigView.ROUTE_NAME, "Shop Config");
        addButtonIfVisible(menuButtonsRow, AbstractProductConfigView.ROUTE_NAME, "Product Config");
        add(menuButtonsRow);
    }
}
