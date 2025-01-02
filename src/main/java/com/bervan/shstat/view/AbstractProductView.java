package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractProductView extends AbstractPageView {
    public static final String ROUTE_NAME = "/shopping/products";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final BervanLogger log;

    public AbstractProductView(ProductViewService productViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        this.log = log;
        Set<String> categories = this.productSearchService.findCategories();
        ComboBox<String> categoryDropdown = new ComboBox<>("Category:");
        categoryDropdown.setItems(categories);

        ComboBox<String> shopDropdown = new ComboBox<>("Shop:");
        shopDropdown.setItems(Arrays.asList("Media Expert", "RTV Euro AGD", "Morele"));

        TextField name = new TextField("Product Name: NOT WORKING YET");

        Button searchButton = new Button("Search");
        VerticalLayout productsLayout = new VerticalLayout();

        searchButton.addClickListener(buttonClickEvent -> {
            SearchApiResponse products = getProductList(categoryDropdown.getValue(), shopDropdown.getValue(), Pageable.ofSize(500));
            productsLayout.removeAll();
            for (Object item : products.getItems()) {
                ProductDTO productDTO = ((ProductDTO) item);

                VerticalLayout productLayout = new VerticalLayout();
                Image image = new Image(productDTO.getImgSrc() == null ? "" : productDTO.getImgSrc(), "No image :(");
                List<PriceDTO> prices = productDTO.getPrices();
                Text priceText = new Text("No price");
                if (prices != null && prices.size() > 0) {
                    priceText = new Text("Price:" + prices.get(0).getPrice());
                }
                productLayout.add(image, new Text(productDTO.getName()), priceText);
                productsLayout.add(productLayout);
            }
        });

        add(shopDropdown, categoryDropdown, name, searchButton, productsLayout);
    }

    public SearchApiResponse getProductList(String category, String shop, Pageable pageable) {
        return productViewService.findProductsByCategory(category, shop, pageable);
    }
}
