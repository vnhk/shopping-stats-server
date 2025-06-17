package com.bervan.shstat.view;

import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseProductsPage extends BaseProductPage {
    protected FlexLayout getProductsLayout(SearchApiResponse products , Map<VerticalLayout, ProductDTO> productCardMap) {
        FlexLayout tileContainer = new FlexLayout();
        tileContainer.setJustifyContentMode(JustifyContentMode.START);
        tileContainer.getStyle().set("display", "flex");
        tileContainer.getStyle().set("flex-wrap", "wrap");
        tileContainer.getStyle().set("gap", "1rem");
        tileContainer.setWidthFull();

        List<ProductDTO> sortedProducts = (List<ProductDTO>) products.getItems().stream()
                .map(item -> (ProductDTO) item)
                .sorted((p1, p2) -> {
                    Double discount1 = ((ProductDTO) p1).getDiscount();
                    Double discount2 = ((ProductDTO) p2).getDiscount();
                    return discount2.compareTo(discount1);
                }).collect(Collectors.toList());

        for (ProductDTO productDTO : sortedProducts) {
            VerticalLayout productCard = new VerticalLayout();
            productCard.setWidth("350px");
            setProductCardStyle(productCard);

            if (productDTO.isActual()) {
                productCard.setClassName("actual-product");
            } else {
                productCard.setClassName("out-of-date-product");
            }

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

            if (!productDTO.isActual()) {
                priceText.setText(priceText.getText() + " - out of date");
            }

            productCard.add(image, nameText, priceText);
            tileContainer.add(productCard);
            productCardMap.put(productCard, productDTO);
        }
        return tileContainer;
    }

    protected abstract String getBackLink(ProductDTO productDTO);

}
