package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.common.BervanComboBox;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractProductsView extends AbstractPageView implements HasUrlParameter<Void> {
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
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele"));

        Set<String> categories = this.productSearchService.findCategories();
        categoryDropdown.setItems(categories);

        VerticalLayout productsLayout = new VerticalLayout();

        searchButton.addClickListener(buttonClickEvent -> {
            SearchApiResponse products = getProductList(categoryDropdown.getValue(), shopDropdown.getValue(), productName.getValue(), Pageable.ofSize(500));
            productsLayout.removeAll();

            FlexLayout tileContainer = new FlexLayout();
            tileContainer.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
            tileContainer.getStyle().set("display", "flex");
            tileContainer.getStyle().set("flex-wrap", "wrap");
            tileContainer.getStyle().set("gap", "1rem");
            tileContainer.setWidthFull();

            for (Object item : products.getItems()) {
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
                        + "&source=" + ROUTE_NAME
                        + "&product-name=" + productName.getValue();
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

        add(shopDropdown, categoryDropdown, productName, searchButton, productsLayout);
    }


    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Void parameter) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        String category = getSingleParam(queryParameters, "category");
        String shop = getSingleParam(queryParameters, "shop");
        String productName = getSingleParam(queryParameters, "product-name");

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
}
