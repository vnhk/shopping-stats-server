package com.bervan.shstat.view;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanComboBox;
import com.bervan.common.component.BervanTextField;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Set;

public abstract class AbstractProductsView extends BaseProductsPage implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/products";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final BervanComboBox<String> shopDropdown = new BervanComboBox<>("Shop:", false);
    private final BervanComboBox<String> categoryDropdown = new BervanComboBox<>("Category:", false);
    private final BervanTextField productName = new BervanTextField("Product Name:");
    private final BervanButton searchButton = new BervanButton("Search");

    public AbstractProductsView(ProductViewService productViewService, ProductSearchService productSearchService) {
        super();
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);
        searchButton.addClickListener(e -> startNewSearch());

        createSearchInterface();
        add(productsLayout);
    }

    private void createSearchInterface() {
        H3 searchTitle = new H3("🛒 Find Offers");
        searchTitle.getStyle().set("margin-top", "-20px").set("color", "var(--lumo-primary-text-color)");

        Div productSection = createSearchSection("Product & Shop & Category",
                createSearchFieldRow(productName, shopDropdown, categoryDropdown));

        HorizontalLayout firstRow = createSearchSectionRow(productSection);
        HorizontalLayout actionButtons = getSearchActionButtonsLayout(searchButton, stopButton);

        add(getSearchForm(searchTitle, actionButtons, firstRow));
    }

    @Override
    protected SearchApiResponse executeSearch(Pageable pageable) {
        return getProductList(categoryDropdown.getValue(), shopDropdown.getValue(), productName.getValue(), pageable);
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