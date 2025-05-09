package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanButton;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.response.ApiResponse;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractBestOffersView extends AbstractPageView implements HasUrlParameter<Void> {
    public static final String ROUTE_NAME = "/shopping/best-offers";
    private final DiscountsViewService discountsViewService;
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


    public AbstractBestOffersView(DiscountsViewService discountsViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.add(new ShoppingLayout(ROUTE_NAME));

        this.discountsViewService = discountsViewService;
        this.productSearchService = productSearchService;
        this.log = log;
        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);

        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele"));

        VerticalLayout productsLayout = new VerticalLayout();

        searchButton = new BervanButton("Search");
        searchButton.addClickListener(buttonClickEvent -> {
            productsLayout.removeAll();
            SearchApiResponse body = (SearchApiResponse) findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.ofSize(50),
                    discountMin.getValue(), discountMax.getValue(), months.getValue(), prevPriceMin.getValue(), prevPriceMax.getValue(), productName.getValue(), categoryDropdown.getValue(), shopDropdown.getValue());

            FlexLayout tileContainer = new FlexLayout();
            tileContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
            tileContainer.getStyle().set("display", "flex");
            tileContainer.getStyle().set("flex-wrap", "wrap");
            tileContainer.getStyle().set("gap", "1rem");
            tileContainer.setWidthFull();

            log.info("Found " + body.getItems().size() + " items for Best Offers View");
            for (Object item : body.getItems()) {
                ProductDTO productDTO = ((ProductDTO) item);

                VerticalLayout productCard = new VerticalLayout();
                productCard.setWidth("350px");
                productCard.getStyle().set("border", "1px solid #ccc");
                productCard.getStyle().set("border-radius", "8px");
                productCard.getStyle().set("padding", "10px");
                productCard.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
                productCard.getStyle().set("background-color", "#fff");
                productCard.getStyle().set("text-align", "center");

                Image image = new Image(productDTO.getImgSrc() == null ? "" : productDTO.getImgSrc(), "No image :(");
                if (productDTO.getImgSrc().startsWith("http") || productDTO.getImgSrc().startsWith("https")) {
                    image.setSrc(productDTO.getImgSrc());
                } else {
                    image.setSrc("data:image/png;base64," + productDTO.getImgSrc());
                }

                image.setWidth("300px");
                image.setHeight("300px");
                image.getStyle().set("object-fit", "contain");

                String link = AbstractProductView.ROUTE_NAME + "/" + productDTO.getId()
                        + "?category=" + categoryDropdown.getValue()
                        + "&shop=" + shopDropdown.getValue()
                        + "&discount-min=" + discountMin.getValue()
                        + "&discount-max=" + discountMax.getValue()
                        + "&months=" + months.getValue()
                        + "&prev-price-min=" + prevPriceMin.getValue()
                        + "&prev-price-max=" + prevPriceMax.getValue()
                        + "&product-name=" + productName.getValue()
                        + "&source=" + ROUTE_NAME;
                Anchor nameText = new Anchor(link, productDTO.getName());

                List<PriceDTO> prices = productDTO.getPrices();
                Text priceText = new Text("No price");
                if (prices != null && !prices.isEmpty()) {
                    priceText = new Text(" Price: " + prices.get(0).getPrice() + " zł");
                }

                productCard.add(image, nameText, priceText);
                tileContainer.add(productCard);
            }

            productsLayout.add(tileContainer);
        });

        add(shopDropdown, categoryDropdown, discountMin, discountMax, months, prevPriceMin, prevPriceMax, productName, searchButton, productsLayout);
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

    private boolean updateField(String fieldValue, AbstractField field, boolean atLeastOneParameter) {
        if (fieldValue != null && !fieldValue.equals("null")) {
            field.setValue(fieldValue);
            atLeastOneParameter = true;
        }
        return atLeastOneParameter;
    }

    private boolean updateFieldWithDefault(Object fieldValue, AbstractField field, boolean atLeastOneParameter, Object defaultVal) {
        if (fieldValue != null && !fieldValue.toString().equals("null")) {
            field.setValue(fieldValue);
            atLeastOneParameter = true;
        } else {
            field.setValue(defaultVal);
        }
        return atLeastOneParameter;
    }

    private Double getDoubleParam(QueryParameters queryParameters, String name) {
        String singleParam = getSingleParam(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Double.valueOf(singleParam);
    }

    private Integer getIntegerParam(QueryParameters queryParameters, String name) {
        String singleParam = getSingleParam(queryParameters, name);
        if (singleParam == null) {
            return null;
        }
        return Integer.valueOf(singleParam);
    }

    private static String getString(String shop) {
        if (shop != null && StringUtils.isBlank(shop.trim())) {
            shop = null;
        }
        return shop;
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
                discountMax, months, category, shop, name, prevPriceMin, prevPriceMax);

    }

    private String getSingleParam(QueryParameters queryParameters, String name) {
        List<String> values = queryParameters.getParameters().get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
