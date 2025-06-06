package com.bervan.shstat.view;

import com.bervan.common.BervanButton;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.queue.RefreshViewService;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.DiscountsViewService;
import com.bervan.shstat.service.ProductSearchService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

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
    private ComboBox<String> categoryDropdown = new ComboBox<>("Category:");
    private BervanButton searchButton;
    private BervanButton rebuildBestOffers;


    public AbstractBestOffersView(DiscountsViewService discountsViewService, RefreshViewService refreshViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.refreshViewService = refreshViewService;
        rebuildBestOffers = new BervanButton("Force Rebuild", (e) -> {
            showPrimaryNotification("Views are rebuilding... It will take time...");
            refreshViewService.refreshViewsScheduled();
            showPrimaryNotification("Views rebuilt");
        });
        this.add(new ShoppingLayout(ROUTE_NAME));

        this.discountsViewService = discountsViewService;
        this.productSearchService = productSearchService;
        this.log = log;
        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);

        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele", "Centrum Rowerowe"));

        VerticalLayout productsLayout = new VerticalLayout();

        searchButton = new BervanButton("Search");
        searchButton.addClickListener(buttonClickEvent -> {
            productsLayout.removeAll();
            SearchApiResponse body = (SearchApiResponse) findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.ofSize(500),
                    discountMin.getValue(), discountMax.getValue(), months.getValue(), prevPriceMin.getValue(), prevPriceMax.getValue(), productName.getValue(), categoryDropdown.getValue(), shopDropdown.getValue());

            FlexLayout tileContainer = getProductsLayout(body);
            productsLayout.add(tileContainer);
        });

        add(rebuildBestOffers, shopDropdown, categoryDropdown, discountMin, discountMax, months, prevPriceMin, prevPriceMax, productName, searchButton, productsLayout);
    }

    @Override
    protected String getBackLink(ProductDTO productDTO) {
        return AbstractProductView.ROUTE_NAME + "/" + productDTO.getId()
                + "?category=" + categoryDropdown.getValue()
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
        String category = getSingleParam(queryParameters, "category");
        String shop = getSingleParam(queryParameters, "shop");
        String productName = getSingleParam(queryParameters, "product-name");
        Double discountMin = getDoubleParam(queryParameters, "discount-min");
        Double discountMax = getDoubleParam(queryParameters, "discount-max");
        Integer months = getIntegerParam(queryParameters, "months");
        Integer prevPriceMin = getIntegerParam(queryParameters, "prev-price-min");
        Integer prevPriceMax = getIntegerParam(queryParameters, "prev-price-max");

        boolean atLeastOneParameter = false;

        atLeastOneParameter = updateField(category, categoryDropdown, atLeastOneParameter);
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

    private ApiResponse findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                                        Double discountMin,
                                                                                        Double discountMax,
                                                                                        Integer months,
                                                                                        Integer prevPriceMin,
                                                                                        Integer prevPriceMax,
                                                                                        String name,
                                                                                        String category,
                                                                                        String shop) {
        category = getString(category);
        shop = getString(shop);
        name = getString(name);

        return discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin,
                discountMax, months, Collections.singletonList(category), shop, name, prevPriceMin, prevPriceMax);

    }
}
