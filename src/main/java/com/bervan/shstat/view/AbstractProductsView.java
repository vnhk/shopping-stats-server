package com.bervan.shstat.view;

import com.bervan.common.BervanComboBox;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractProductsView extends BaseProductsPage implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/products";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final BervanLogger log;
    private final ComboBox<String> shopDropdown = new BervanComboBox<>("Shop:");
    private final ComboBox<String> categoryDropdown = new ComboBox<>("Category:");
    private final TextField productName = new TextField("Product Name:");
    private final Button searchButton = new Button("Search");

    public AbstractProductsView(ProductViewService productViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        this.log = log;
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);

        VerticalLayout productsLayout = new VerticalLayout();

        searchButton.addClickListener(buttonClickEvent -> {
            SearchApiResponse products = getProductList(categoryDropdown.getValue(), shopDropdown.getValue(), productName.getValue(), Pageable.ofSize(500));
            productsLayout.removeAll();

            Map<VerticalLayout, ProductDTO> productCardMap = new HashMap<>();
            FlexLayout tileContainer = getProductsLayout(products, productCardMap);

            Checkbox showOnlyActualCheckbox = new Checkbox("Show only actual products");
            showOnlyActualCheckbox.addValueChangeListener(event -> {
                boolean onlyActual = event.getValue();
                productCardMap.forEach((productCard, dto) -> {
                    productCard.setVisible(!onlyActual || dto.isActual());
                });
            });

            productsLayout.add(showOnlyActualCheckbox, tileContainer);
        });

        createSearchInterface();
        add(productsLayout);
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
