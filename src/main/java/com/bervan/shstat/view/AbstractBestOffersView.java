package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.SearchService;
import com.bervan.shstat.response.ApiResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractBestOffersView extends AbstractPageView {
    public static final String ROUTE_NAME = "/shopping/best-offers";
    private final DiscountsViewService discountsViewService;
    private final SearchService searchService;
    private final BervanLogger log;

    public AbstractBestOffersView(DiscountsViewService discountsViewService, SearchService searchService, BervanLogger log) {
        super();
        this.discountsViewService = discountsViewService;
        this.searchService = searchService;
        this.log = log;
        Set<String> categories = this.searchService.findCategories();
        ComboBox<String> categoryDropdown = new ComboBox<>("Category:");
        categoryDropdown.setItems(categories);

        ComboBox<String> shopDropdown = new ComboBox<>("Shop:");
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele"));

        IntegerField discountMin = new IntegerField("Discount Min:");
        IntegerField discountMax = new IntegerField("Discount Max:");
        IntegerField months = new IntegerField("Number of months:");
        IntegerField prevPriceMin = new IntegerField("Previous Price Min:");
        IntegerField prevPriceMax = new IntegerField("Previous Price Max:");

        TextField name = new TextField("Product Name:");

        Button searchButton = new Button("Search");
        searchButton.addClickListener(buttonClickEvent -> {
            findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.unpaged(),
                    discountMin.getValue().toString(), discountMax.getValue().toString(), months.getValue(),
                    prevPriceMin.getValue(), prevPriceMax.getValue(), name.getValue(), categoryDropdown.getValue(),
                    shopDropdown.getValue());
        });

        add(shopDropdown, categoryDropdown, discountMin, discountMax, prevPriceMin, prevPriceMax, name, searchButton);
    }

    private ResponseEntity<ApiResponse> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                                        String discountMin,
                                                                                        String discountMax,
                                                                                        Integer months,
                                                                                        Integer prevPriceMin,
                                                                                        Integer prevPriceMax,
                                                                                        String name,
                                                                                        String category,
                                                                                        String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }

        if (discountMin.endsWith("%") && discountMax.endsWith("%")) {
            String numberMin = discountMin.split("%")[0];
            String numberMax = discountMax.split("%")[0];
            return ResponseEntity.ok(discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, Double.parseDouble(numberMin),
                    Double.parseDouble(numberMax), months, category, shop, name, prevPriceMin, prevPriceMax));
        } else {
            return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage.\nThe months must be positive."))
                    , HttpStatus.BAD_REQUEST);
        }
    }
}
