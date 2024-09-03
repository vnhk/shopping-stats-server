package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.SearchService;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
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

        NumberField discountMin = new NumberField("Discount Min:");
        discountMin.setValue(1.0);
        NumberField discountMax = new NumberField("Discount Max:");
        discountMax.setValue(100.0);
        IntegerField months = new IntegerField("Number of months:");
        months.setValue(12);
        IntegerField prevPriceMin = new IntegerField("Previous Price Min:");
        prevPriceMin.setValue(10);
        IntegerField prevPriceMax = new IntegerField("Previous Price Max:");
        prevPriceMax.setValue(10000);

        TextField name = new TextField("Product Name:");

        Button searchButton = new Button("Search");
        searchButton.addClickListener(buttonClickEvent -> {
            ResponseEntity<ApiResponse> discountsComparedToAVGOnPricesInLastXMonths = findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.ofSize(50),
                    discountMin.getValue(), discountMax.getValue(), months.getValue(), prevPriceMin.getValue(), prevPriceMax.getValue(), name.getValue(), categoryDropdown.getValue(), shopDropdown.getValue());

            SearchApiResponse body = (SearchApiResponse) discountsComparedToAVGOnPricesInLastXMonths.getBody();
            for (Object item : body.getItems()) {
                ProductDTO productDTO = ((ProductDTO) item);

                VerticalLayout productLayout = new VerticalLayout();
                Image image = new Image(productDTO.getImgSrc(), "No image :(");
                productLayout.add(image, new Text(productDTO.getName()), new Text("Price:" + productDTO.getPrices().get(0).getPrice()));
            }
        });

        add(shopDropdown, categoryDropdown, discountMin, discountMax, months, prevPriceMin, prevPriceMax, name, searchButton);
    }

    private ResponseEntity<ApiResponse> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                                        Double discountMin,
                                                                                        Double discountMax,
                                                                                        Integer months,
                                                                                        Integer prevPriceMin,
                                                                                        Integer prevPriceMax,
                                                                                        String name,
                                                                                        String category,
                                                                                        String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }

        return ResponseEntity.ok(discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin,
                discountMax, months, category, shop, name, prevPriceMin, prevPriceMax));

    }
}
