package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.util.List;

public abstract class BaseProductsPage extends BaseProductPage {
    protected FlexLayout getProductsLayout(SearchApiResponse products) {
        FlexLayout tileContainer = new FlexLayout();
        tileContainer.setJustifyContentMode(JustifyContentMode.START);
        tileContainer.getStyle().set("display", "flex");
        tileContainer.getStyle().set("flex-wrap", "wrap");
        tileContainer.getStyle().set("gap", "1rem");
        tileContainer.setWidthFull();

        for (Object item : products.getItems()) {
            ProductDTO productDTO = ((ProductDTO) item);

            VerticalLayout productCard = new VerticalLayout();
            productCard.setWidth("350px");
            setProductCardStyle(productCard);

            Image image = getProductImage(productDTO);

            image.setWidth("300px");
            image.setHeight("300px");
            image.getStyle().set("object-fit", "contain");

            String link = getBackLink(productDTO);
            Anchor nameText = new Anchor(link, productDTO.getName());

            List<PriceDTO> prices = productDTO.getPrices();
            Text priceText = new Text("No price");
            if (prices != null && !prices.isEmpty()) {
                priceText = getLatestPriceText(prices, productDTO);
            }

            productCard.add(image, nameText, priceText);
            tileContainer.add(productCard);
        }
        return tileContainer;
    }

    protected abstract String getBackLink(ProductDTO productDTO);

}
