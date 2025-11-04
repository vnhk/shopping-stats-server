package com.bervan.shstat.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanDynamicMultiDropdownController;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.queue.RefreshViewService;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.DiscountsViewService;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractBestOffersView extends BaseProductsPage implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/best-offers";
    private final DiscountsViewService discountsViewService;
    private final RefreshViewService refreshViewService;
    private final AsyncTaskService asyncTaskService;
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

    public AbstractBestOffersView(DiscountsViewService discountsViewService, RefreshViewService refreshViewService, AsyncTaskService asyncTaskService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.refreshViewService = refreshViewService;
        this.discountsViewService = discountsViewService;
        this.asyncTaskService = asyncTaskService;
        this.productSearchService = productSearchService;
        this.log = log;

        initializeComponents();
        createSearchInterface();
    }

    private void initializeComponents() {
        rebuildBestOffers = new BervanButton("Force Rebuild", (e) -> {
            AsyncTask newAsyncTask = asyncTaskService.createAndStoreAsyncTask();
            showPrimaryNotification("Views are rebuilding... It will take time... You will be notified!");

            SecurityContext context = SecurityContextHolder.getContext();
            new Thread(() -> {
                SecurityContextHolder.setContext(context);
                AsyncTask asyncTask = asyncTaskService.setInProgress(newAsyncTask, "Manual Product Views Refresh started!");
                try {
                    refreshViewService.refreshViewsScheduled();
                    asyncTaskService.setFinished(newAsyncTask, "Manual Product Views Refresh finished!");
                } catch (Exception ex) {
                    asyncTaskService.setFailed(asyncTask, ex.getMessage());
                }
            }).start();

        });

        this.add(new ShoppingLayout(ROUTE_NAME));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown = new BervanDynamicMultiDropdownController("Categories", "Categories:",
                categories, new ArrayList<>(), false);

        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));
        shopDropdown.setPlaceholder("Select shop...");

        configureSearchFields();
        createSearchButton();
        add(productsLayout);
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
        searchButton.addClickListener(buttonClickEvent -> startNewSearch());
    }

    private void createSearchInterface() {
        H3 searchTitle = new H3("🎯 Find Best Offers");
        searchTitle.getStyle().set("margin-top", "0")
                .set("color", "var(--lumo-primary-text-color)");

        Div productSection = createSearchSection("Product & Shop",
                createSearchFieldRow(productName, shopDropdown));

        Div categorySection = createSearchSection("Categories", createSearchFieldRow(categoryDropdown));

        // First row - Product & Categories
        HorizontalLayout firstRow = createSearchSectionRow(productSection, categorySection);

        Div discountSection = createSearchSection("Discount Range",
                createSearchFieldRow(discountMin, discountMax),
                createSearchFieldRow(months));

        Div priceSection = createSearchSection("Previous Price Range",
                createSearchFieldRow(prevPriceMin, prevPriceMax));

        // Second row - Discount & Price Range
        HorizontalLayout secondRow = createSearchSectionRow(discountSection, priceSection);

        HorizontalLayout actionButtons = getSearchActionButtonsLayout(searchButton, rebuildBestOffers);

        add(getSearchForm(searchTitle, actionButtons, firstRow, secondRow), productsLayout);
    }

    @Override
    protected SearchApiResponse executeSearch(Pageable pageable) {
        return (SearchApiResponse) findDiscountsComparedToAVGOnPricesInLastXMonths(pageable,
                discountMin.getValue(), discountMax.getValue(), months.getValue(),
                prevPriceMin.getValue(), prevPriceMax.getValue(), productName.getValue(),
                categoryDropdown.getValue(), shopDropdown.getValue());
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
        Object category = getParams(queryParameters, "category");
        List<String> categories = getCategories(category);
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

    private List<String> getCategories(Object category) {
        List<String> categories = new ArrayList<>();

        if (category instanceof String s) {
            categories.add(s);
        } else if (category instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    categories.add(str);
                }
            }
        }
        return categories;
    }

    private boolean updateField(List<String> fieldValues, BervanDynamicMultiDropdownController multiDropdown, boolean atLeastOneParameter) {
        if (fieldValues != null && !fieldValues.isEmpty() && multiDropdown != null) {
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
