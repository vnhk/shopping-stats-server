package com.bervan.shstat.view;

import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseProductsPage extends BaseProductPage {
    protected FlexLayout getProductsLayout(SearchApiResponse products, Map<VerticalLayout, ProductDTO> productCardMap) {
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

    protected Div createSearchSection(String title, Object... components) {
        Div section = new Div();
        section.getStyle()
                .set("margin-bottom", "0")
                .set("padding", "0.5rem")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("height", "fit-content")
                .set("min-width", "0"); // Allows flex items to shrink

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        section.add(sectionTitle);
        for (Object component : components) {
            section.add((Component) component);
        }

        section.getStyle().set("flex", "1");

        return section;
    }

    protected HorizontalLayout createSearchSectionRow(Div... divs) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.add(divs);
        return row;
    }

    protected HorizontalLayout getSearchActionButtonsLayout(Button... buttons) {
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.add(buttons);
        actionButtons.setSpacing(true);
        actionButtons.setJustifyContentMode(JustifyContentMode.CENTER);
        actionButtons.getStyle().set("margin-top", "1rem");
        return actionButtons;
    }

    protected Div getSearchForm(Component titleComponent, HorizontalLayout actionButtons, HorizontalLayout... formSearchSectionRows) {
        VerticalLayout searchForm = new VerticalLayout();
        searchForm.addClassName("search-form");
        searchForm.setSpacing(true);
        searchForm.setPadding(false);
        searchForm.setWidthFull();
        searchForm.add(formSearchSectionRows);
        searchForm.add(actionButtons);

        Div searchContainer = new Div();
        searchContainer.addClassName("search-container");
        searchContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "2rem")
                .set("width", "100%");

        if (titleComponent != null) {
            searchContainer.add(titleComponent, searchForm);
        } else {
            searchContainer.add(searchForm);
        }

        return searchContainer;
    }

    protected HorizontalLayout createFieldRow(Object... fields) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        for (Object field : fields) {
            Component component = (Component) field;
            component.getElement().getStyle().set("flex", "1");
            row.add(component);
        }

        return row;
    }

    protected abstract String getBackLink(ProductDTO productDTO);

}
