package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractProductView extends AbstractPageView implements HasUrlParameter<Long> {
    public static final String ROUTE_NAME = "/shopping/product/:productId";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final BervanLogger log;

    public AbstractProductView(ProductViewService productViewService, ProductSearchService productSearchService, BervanLogger log) {
        super();
        this.add(new ShoppingLayout(ROUTE_NAME));
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        this.log = log;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long productId) {
        String backLink = buildBackLink(beforeEvent);

        SearchApiResponse byId = productViewService.findById(productId, Pageable.ofSize(1));
        ProductDTO productDTO = (ProductDTO) byId.getItems().iterator().next();

        /// /////////////////////
        VerticalLayout productsLayout = new VerticalLayout();

        VerticalLayout productCard = new VerticalLayout();
        productCard.setWidth("1200px");
        productCard.getStyle().set("border", "1px solid #ccc");
        productCard.getStyle().set("border-radius", "8px");
        productCard.getStyle().set("padding", "10px");
        productCard.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
        productCard.getStyle().set("background-color", "#fff");
        productCard.getStyle().set("text-align", "center");


        Anchor backButton = new Anchor(backLink, "← Back to products");
        productCard.add(backButton);

        Image image = new Image(productDTO.getImgSrc() == null ? "" : productDTO.getImgSrc(), "No image :(");
        if (productDTO.getImgSrc().startsWith("http") || productDTO.getImgSrc().startsWith("https")) {
            image.setSrc(productDTO.getImgSrc());
        } else {
            image.setSrc("data:image/png;base64," + productDTO.getImgSrc());
        }

        image.setWidth("400px");
        image.setHeight("400px");
        image.getStyle().set("object-fit", "contain");

        productCard.add(image, new H3(productDTO.getName()));

        List<PriceDTO> prices = productDTO.getPrices();
        Text priceText = new Text("No price");
        if (prices != null && !prices.isEmpty()) {
            priceText = new Text(" Price: " + prices.get(0).getPrice() + " zł");
            productCard.add(priceText);

            productCard.add(new Hr());
            productCard.add(new H3("Last 10 prices:"));

            for (int i = 1; i < prices.size() && i < 10; i++) {
                Text anotherPriceText = new Text(" Price: " + prices.get(i).getPrice() + " zł (" + prices.get(i).getFormattedDate() + ")");
                productCard.add(anotherPriceText);
                productCard.add(new Hr());
            }

            prices.sort(Comparator.comparing(PriceDTO::getDate));
            ProductPriceChart productPriceChart = new ProductPriceChart(
                    prices.stream().map(PriceDTO::getDate).map(e -> new SimpleDateFormat("dd-MM-yyyy").format(e)).toList(),
                    prices.stream().map(PriceDTO::getPrice).map(e -> Double.parseDouble(e.toPlainString()))
                            .toList()
            );
            productCard.add(productPriceChart);
        } else {
            productCard.add(priceText);
        }

        productsLayout.add(productCard);

        add(productsLayout);
    }

    private String buildBackLink(BeforeEvent beforeEvent) {
        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        String source = getSingleParam(queryParameters, "source");

        Map<String, String> params = queryParameters.getParameters()
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals("source"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> getSingleParam(queryParameters, e.getKey())
                ));

        String paramString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return source + "?" + paramString;
    }
}
