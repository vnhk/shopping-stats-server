package com.bervan.shstat.view;

import com.bervan.common.BervanButton;
import com.bervan.common.BervanDynamicMultiDropdownController;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.queue.RefreshViewService;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.DiscountsViewService;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractBestOffersView extends BaseProductsPage implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/best-offers";
    private final DiscountsViewService discountsViewService;
    private final RefreshViewService refreshViewService;
    private final ProductSearchService productSearchService;
    private final BervanLogger log;
    private NumberField discountMin = new NumberField("Discount Min:");
    private NumberField discountMax = new NumberField("Discount Max:");
    private IntegerField months = new IntegerField("Number of months:");
    private IntegerField prevPriceMin = new IntegerField("Previous Price Min:");
    private IntegerField prevPriceMax = new IntegerField("Previous Price Max:");
    private TextField productName = new TextField("Product Name:");
    private ComboBox<String> shopDropdown = new ComboBox<>("Shop:");
    private BervanDynamicMultiDropdownController categoryDropdown;
    private BervanButton searchButton;
    private BervanButton rebuildBestOffers;
    private VerticalLayout productsLayout;

    public AbstractBestOffersView(DiscountsViewService discountsViewService, RefreshViewService refreshViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.refreshViewService = refreshViewService;
        this.discountsViewService = discountsViewService;
        this.productSearchService = productSearchService;
        this.log = log;

        initializeComponents();
        createSearchInterface();
    }

    private void initializeComponents() {
        rebuildBestOffers = new BervanButton("Force Rebuild", (e) -> {
            showPrimaryNotification("Views are rebuilding... It will take time...");
            refreshViewService.refreshViewsScheduled();
            showPrimaryNotification("Views rebuilt");
        });

        this.add(new ShoppingLayout(ROUTE_NAME));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown = new BervanDynamicMultiDropdownController("Categories", "Categories:",
                categories, new ArrayList<>());

        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));
        shopDropdown.setPlaceholder("Select shop...");

        productsLayout = new VerticalLayout();

        configureSearchFields();
        createSearchButton();
    }

    private void configureSearchFields() {
        productName.setPlaceholder("Search by product name...");
        productName.setClearButtonVisible(true);
        productName.setWidthFull();

        discountMin.setPlaceholder("e.g. 20");
        discountMax.setPlaceholder("e.g. 100");
        discountMin.setSuffixComponent(new Div("%"));
        discountMax.setSuffixComponent(new Div("%"));

        months.setPlaceholder("e.g. 3");
        months.setMin(1);
        months.setMax(24);

        prevPriceMin.setPlaceholder("e.g. 100");
        prevPriceMax.setPlaceholder("e.g. 10000");
        prevPriceMin.setSuffixComponent(new Div("zł"));
        prevPriceMax.setSuffixComponent(new Div("zł"));
    }

    private void createSearchButton() {
        searchButton = new BervanButton("🔍 Search Best Offers");
        searchButton.addClickListener(buttonClickEvent -> performSearch());
    }

    private void createSearchInterface() {
        Div searchContainer = new Div();
        searchContainer.addClassName("search-container");
        searchContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "2rem")
                .set("width", "100%");

        H3 searchTitle = new H3("🎯 Find Best Offers");
        searchTitle.getStyle().set("margin-top", "-20px").set("color", "var(--lumo-primary-text-color)");

        // Create grid layout for search sections
        HorizontalLayout searchGrid = new HorizontalLayout();
        searchGrid.setWidthFull();
        searchGrid.setSpacing(true);
        searchGrid.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // First row - Product & Categories
        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setWidthFull();
        firstRow.setSpacing(true);

        Div productSection = createSearchSection("Product & Shop",
                createFieldRow(productName, shopDropdown));
        productSection.getStyle().set("flex", "1");

        Div categorySection = createSearchSection("Categories", createFieldRow(categoryDropdown));
        categorySection.getStyle().set("flex", "1");

        firstRow.add(productSection, categorySection);

        // Second row - Discount & Price Range
        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setWidthFull();
        secondRow.setSpacing(true);

        Div discountSection = createSearchSection("Discount Range",
                createFieldRow(discountMin, discountMax),
                createFieldRow(months));
        discountSection.getStyle().set("flex", "1");

        Div priceSection = createSearchSection("Previous Price Range",
                createFieldRow(prevPriceMin, prevPriceMax));
        priceSection.getStyle().set("flex", "1");

        secondRow.add(discountSection, priceSection);

        // Action Buttons - centered
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.add(searchButton, rebuildBestOffers);
        actionButtons.setSpacing(true);
        actionButtons.setJustifyContentMode(JustifyContentMode.CENTER);
        actionButtons.getStyle().set("margin-top", "1rem");

        VerticalLayout searchForm = new VerticalLayout();
        searchForm.setSpacing(true);
        searchForm.setPadding(false);
        searchForm.setWidthFull();
        searchForm.add(firstRow, secondRow, actionButtons);

        searchContainer.add(searchTitle, searchForm);

        add(searchContainer, productsLayout);
    }

    private Div createSearchSection(String title, Object... components) {
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
            section.add((com.vaadin.flow.component.Component) component);
        }

        return section;
    }

    private HorizontalLayout createFieldRow(Object... fields) {
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

    private void performSearch() {
        productsLayout.removeAll();
        SearchApiResponse products = (SearchApiResponse) findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.ofSize(500),
                discountMin.getValue(), discountMax.getValue(), months.getValue(),
                prevPriceMin.getValue(), prevPriceMax.getValue(), productName.getValue(),
                categoryDropdown.getValue(), shopDropdown.getValue());

        Map<VerticalLayout, ProductDTO> productCardMap = new HashMap<>();
        FlexLayout tileContainer = getProductsLayout(products, productCardMap);

        Checkbox showOnlyActualCheckbox = new Checkbox("Show only actual products");
        showOnlyActualCheckbox.getStyle().set("margin-bottom", "1rem");
        showOnlyActualCheckbox.addValueChangeListener(event -> {
            boolean onlyActual = event.getValue();
            productCardMap.forEach((productCard, dto) -> {
                productCard.setVisible(!onlyActual || dto.isActual());
            });
        });

        productsLayout.add(showOnlyActualCheckbox, tileContainer);
    }

    @Override
    protected String getBackLink(ProductDTO productDTO) {
        String categories = categoryDropdown.getValue().stream()
                .map(cat -> "category=" + URLEncoder.encode(cat, StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return AbstractProductView.ROUTE_NAME + "/" + productDTO.getId()
                + "?" + categories
                + "&shop=" + shopDropdown.getValue()
                + "&discount-min=" + discountMin.getValue()
                + "&discount-max=" + discountMax.getValue()
                + "&months=" + months.getValue()
                + "&prev-price-min=" + prevPriceMin.getValue()
                + "&prev-price-max=" + prevPriceMax.getValue()
                + "&product-name=" + productName.getValue()
                + "&source=" + ROUTE_NAME;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Void parameter) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        List<String> categories = (List<String>) getParams(queryParameters, "category");
        String shop = (String) getParams(queryParameters, "shop");
        String productName = (String) getParams(queryParameters, "product-name");
        Double discountMin = getDoubleParam(queryParameters, "discount-min");
        Double discountMax = getDoubleParam(queryParameters, "discount-max");
        Integer months = getIntegerParam(queryParameters, "months");
        Integer prevPriceMin = getIntegerParam(queryParameters, "prev-price-min");
        Integer prevPriceMax = getIntegerParam(queryParameters, "prev-price-max");

        boolean atLeastOneParameter = false;

        atLeastOneParameter = updateField(categories, categoryDropdown, atLeastOneParameter);
        atLeastOneParameter = updateField(shop, shopDropdown, atLeastOneParameter);
        atLeastOneParameter = updateField(productName, this.productName, atLeastOneParameter);

        atLeastOneParameter = updateFieldWithDefault(discountMin, this.discountMin, atLeastOneParameter, 20.0);
        atLeastOneParameter = updateFieldWithDefault(discountMax, this.discountMax, atLeastOneParameter, 100.0);
        atLeastOneParameter = updateFieldWithDefault(months, this.months, atLeastOneParameter, 3);
        atLeastOneParameter = updateFieldWithDefault(prevPriceMin, this.prevPriceMin, atLeastOneParameter, 100);
        atLeastOneParameter = updateFieldWithDefault(prevPriceMax, this.prevPriceMax, atLeastOneParameter, 10000);

        if (atLeastOneParameter) {
            searchButton.click();
        }
    }

    private boolean updateField(List<String> fieldValues, BervanDynamicMultiDropdownController multiDropdown, boolean atLeastOneParameter) {
        if (fieldValues != null) {
            multiDropdown.setValue(fieldValues);
            atLeastOneParameter = true;
        }

        return atLeastOneParameter;
    }

    private ApiResponse findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                        Double discountMin,
                                                                        Double discountMax,
                                                                        Integer months,
                                                                        Integer prevPriceMin,
                                                                        Integer prevPriceMax,
                                                                        String name,
                                                                        List<String> category,
                                                                        String shop) {
        shop = getString(shop);
        name = getString(name);

        return discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin,
                discountMax, months, category, shop, name, prevPriceMin, prevPriceMax);

    }
}
