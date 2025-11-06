package com.bervan.shstat.view;

import com.bervan.common.component.BervanButton;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BaseProductsPage extends BaseProductPage {
    protected static final int PAGE_SIZE = 10;
    protected static final int MAX_PRODUCTS = 500;
    protected int currentPage = 0;
    protected boolean loading = false;
    protected boolean allLoaded = false;
    protected boolean stoppedByUser = false;
    protected int totalLoaded = 0;
    protected FlexLayout tileContainer;
    protected Map<VerticalLayout, ProductDTO> productCardMap;
    protected Checkbox showOnlyActualCheckbox;
    protected BervanButton stopButton;
    protected VerticalLayout productsLayout;

    public BaseProductsPage() {
        productsLayout = new VerticalLayout();
        productsLayout.setWidthFull();
        stopButton = new BervanButton("Stop loading");
        stopButton.setVisible(false);
        stopButton.addClickListener(e -> {
            stoppedByUser = true;
            showSuccessNotification("Loading stopped by user.");
            stopButton.setVisible(false);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        stoppedByUser = true;
        super.onDetach(detachEvent);
    }

    protected void startNewSearch() {
        // Reset state
        stopButton.setVisible(true);
        currentPage = 0;
        totalLoaded = 0;
        stoppedByUser = false;
        loading = false;
        allLoaded = false;
        productsLayout.removeAll();

        productCardMap = new HashMap<>();
        tileContainer = getProductSearchResultFlexLayout();

        showOnlyActualCheckbox = new Checkbox("Show only actual products");
        showOnlyActualCheckbox.addValueChangeListener(event -> {
            boolean onlyActual = event.getValue();
            productCardMap.forEach((productCard, dto) ->
                    productCard.setVisible(!onlyActual || dto.isActual()));
        });

        productsLayout.add(new HorizontalLayout(showOnlyActualCheckbox), tileContainer);

        // Start async loading
        loadNextPageAsync();
    }

    protected void loadNextPageAsync() {
        if (loading || allLoaded || stoppedByUser) {
            return;
        }

        if (totalLoaded >= MAX_PRODUCTS) {
            allLoaded = true;
            showWarningNotification("There are more than 500 products — please update filters.");
            stopButton.setVisible(false);
            return;
        }

        loading = true;
        Pageable pageable = PageRequest.of(currentPage, PAGE_SIZE);

        FlexLayout placeholders = createLoadingPlaceholders(PAGE_SIZE);
        tileContainer.add(placeholders);

        CompletableFuture
                .supplyAsync(() -> executeSearch(pageable))
                .thenAccept(response -> getUI().ifPresent(ui -> ui.access(() -> {
                    tileContainer.remove(placeholders);

                    if (stoppedByUser) {
                        loading = false;
                        return;
                    }

                    if (response.getItems().isEmpty()) {
                        allLoaded = true;
                        loading = false;
                        return;
                    }

                    // Create new cards
                    List<VerticalLayout> newProducts = getProductsLayouts(response, productCardMap);
                    newProducts.forEach(c -> c.getElement().getClassList().add("fade-in"));
                    for (VerticalLayout productCard : newProducts) {
                        tileContainer.add(productCard);
                    }

                    totalLoaded += response.getItems().size();
                    currentPage++;

                    if (totalLoaded >= MAX_PRODUCTS) {
                        allLoaded = true;
                        showWarningNotification("There are more than 500 products — please update filters.");
                        loading = false;
                        return;
                    }

                    if (response.getItems().size() < PAGE_SIZE) {
                        allLoaded = true;
                        loading = false;
                        return;
                    }

                    loading = false;
                    loadNextPageAsync(); // continue loading next batch
                })));
    }

    protected abstract SearchApiResponse executeSearch(Pageable pageable);

    protected FlexLayout createLoadingPlaceholders(int PAGE_SIZE) {
        FlexLayout placeholders = getProductSearchResultFlexLayout();
        for (int i = 0; i < PAGE_SIZE; i++) {
            VerticalLayout productCard = getProductCard();
            productCard.addClassName("loading-card");
            placeholders.add(productCard);
        }
        return placeholders;
    }


    protected List<VerticalLayout> getProductsLayouts(SearchApiResponse products, Map<VerticalLayout, ProductDTO> productCardMap) {
        List<VerticalLayout> cards = new ArrayList<>();
        List<ProductDTO> sortedProducts = (List<ProductDTO>) products.getItems().stream()
                .map(item -> (ProductDTO) item)
                .sorted((p1, p2) -> {
                    Double discount1 = ((ProductDTO) p1).getDiscount();
                    Double discount2 = ((ProductDTO) p2).getDiscount();
                    return discount2.compareTo(discount1);
                }).collect(Collectors.toList());

        for (ProductDTO productDTO : sortedProducts) {
            VerticalLayout productCard = getProductCard();

            if (productDTO.isActual()) {
                productCard.setClassName("actual-product");
            } else {
                productCard.setClassName("out-of-date-product");
            }

            Image image = getProductImage(productDTO);

            image.setWidth("200px");
            image.setHeight("200px");
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
            cards.add(productCard);
            productCardMap.put(productCard, productDTO);
        }
        return cards;
    }

    protected FlexLayout getProductSearchResultFlexLayout() {
        FlexLayout tileContainer = new FlexLayout();
        tileContainer.setJustifyContentMode(JustifyContentMode.START);
        tileContainer.getStyle().set("display", "flex");
        tileContainer.getStyle().set("flex-wrap", "wrap");
        tileContainer.getStyle().set("gap", "1rem");
        tileContainer.setWidthFull();
        return tileContainer;
    }

    protected VerticalLayout getProductCard() {
        VerticalLayout productCard = new VerticalLayout();
        productCard.setWidth("350px");
        productCard.setHeight("460px");
        setProductCardStyle(productCard);
        return productCard;
    }

    protected abstract String getBackLink(ProductDTO productDTO);

}
