package com.bervan.shstat.view;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.component.BervanTextField;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractProductsView extends BaseProductsPage implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/products";
    // --- pagination state ---
    private static final int PAGE_SIZE = 10;
    private static final int MAX_PRODUCTS = 500;
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final BervanComboBox<String> shopDropdown = new BervanComboBox<>("Shop:", false);
    private final BervanComboBox<String> categoryDropdown = new BervanComboBox<>("Category:", false);
    private final BervanTextField productName = new BervanTextField("Product Name:");
    private final BervanButton searchButton = new BervanButton("Search");
    private int currentPage = 0;
    private boolean loading = false;
    private boolean allLoaded = false;
    private boolean stoppedByUser = false;
    private int totalLoaded = 0;

    private VerticalLayout productsLayout;
    private FlexLayout tileContainer;
    private Map<VerticalLayout, ProductDTO> productCardMap;
    private Checkbox showOnlyActualCheckbox;
    private BervanButton stopButton;

    public AbstractProductsView(ProductViewService productViewService, ProductSearchService productSearchService) {
        super();
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);

        productsLayout = new VerticalLayout();
        productsLayout.setWidthFull();

        searchButton.addClickListener(e -> startNewSearch());

        createSearchInterface();
        add(productsLayout);
    }

    private void startNewSearch() {
        // Reset state
        currentPage = 0;
        totalLoaded = 0;
        stoppedByUser = false;
        loading = false;
        allLoaded = false;
        productsLayout.removeAll();

        productCardMap = new HashMap<>();
        tileContainer = new FlexLayout();
        tileContainer.getStyle().set("flex-wrap", "wrap");

        showOnlyActualCheckbox = new Checkbox("Show only actual products");
        showOnlyActualCheckbox.addValueChangeListener(event -> {
            boolean onlyActual = event.getValue();
            productCardMap.forEach((productCard, dto) ->
                    productCard.setVisible(!onlyActual || dto.isActual()));
        });

        stopButton = new BervanButton("Stop loading");
        stopButton.addClickListener(e -> {
            stoppedByUser = true;
            showSuccessNotification("Loading stopped by user.");
        });

        productsLayout.add(new HorizontalLayout(showOnlyActualCheckbox, stopButton), tileContainer);

        // Start async loading
        loadNextPageAsync();
    }

    private void loadNextPageAsync() {
        if (loading || allLoaded || stoppedByUser) {
            return;
        }

        if (totalLoaded >= MAX_PRODUCTS) {
            allLoaded = true;
            showWarningNotification("There are more than 500 products — please update filters.");
            return;
        }

        loading = true;
        Pageable pageable = PageRequest.of(currentPage, PAGE_SIZE);

        // Load asynchronously to avoid blocking UI
        CompletableFuture
                .supplyAsync(() -> getProductList(categoryDropdown.getValue(), shopDropdown.getValue(), productName.getValue(), pageable))
                .thenAccept(response -> getUI().ifPresent(ui -> ui.access(() -> {
                    if (stoppedByUser) {
                        loading = false;
                        return;
                    }

                    if (response.getItems().isEmpty()) {
                        allLoaded = true;
                        loading = false;
                        return;
                    }

                    FlexLayout newProducts = getProductsLayout(response, productCardMap);
                    tileContainer.add(newProducts);
                    totalLoaded += response.getItems().size();
                    currentPage++;

                    // Stop if we hit the max limit
                    if (totalLoaded >= MAX_PRODUCTS) {
                        allLoaded = true;
                        showWarningNotification("There are more than 500 products — please update filters.");
                        loading = false;
                        return;
                    }

                    // Stop if no more products
                    if (response.getItems().size() < PAGE_SIZE) {
                        allLoaded = true;
                        loading = false;
                        return;
                    }

                    loading = false;

                    // Continue loading next page automatically
                    loadNextPageAsync();
                })));
    }

    private void createSearchInterface() {
        H3 searchTitle = new H3("🛒 Find Offers");
        searchTitle.getStyle().set("margin-top", "-20px").set("color", "var(--lumo-primary-text-color)");

        Div productSection = createSearchSection("Product & Shop & Category",
                createSearchFieldRow(productName, shopDropdown, categoryDropdown));

        HorizontalLayout firstRow = createSearchSectionRow(productSection);
        HorizontalLayout actionButtons = getSearchActionButtonsLayout(searchButton);

        add(getSearchForm(searchTitle, actionButtons, firstRow));
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Void parameter) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        String category = (String) getParams(queryParameters, "category");
        String shop = (String) getParams(queryParameters, "shop");
        String productName = (String) getParams(queryParameters, "product-name");

        boolean atLeastOneParameter = false;
        atLeastOneParameter = updateField(category, categoryDropdown, atLeastOneParameter);
        atLeastOneParameter = updateField(shop, shopDropdown, atLeastOneParameter);
        atLeastOneParameter = updateField(productName, this.productName, atLeastOneParameter);

        if (atLeastOneParameter) {
            searchButton.click();
        }
    }

    public SearchApiResponse getProductList(String category, String shop, String productName, Pageable pageable) {
        return productViewService.findProducts(category, shop, productName, pageable);
    }

    @Override
    protected String getBackLink(ProductDTO productDTO) {
        return AbstractProductView.ROUTE_NAME + "/" + productDTO.getId()
                + "?category=" + categoryDropdown.getValue()
                + "&shop=" + shopDropdown.getValue()
                + "&source=" + ROUTE_NAME
                + "&product-name=" + productName.getValue();
    }
}