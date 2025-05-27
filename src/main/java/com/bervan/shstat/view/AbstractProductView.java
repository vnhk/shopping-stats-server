package com.bervan.shstat.view;

import com.bervan.common.user.UserRepository;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductBasedOnDateAttributesService;
import com.bervan.shstat.ProductSearchService;
import com.bervan.shstat.ProductService;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractProductView extends BaseProductPage implements HasUrlParameter<Long> {
    public static final String ROUTE_NAME = "/shopping/product/:productId";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductSimilarOffersService productSimilarOffersService;
    private final ProductRepository productRepository;
    private final ProductBasedOnDateAttributesService productDateAttService;
    private final BervanLogger log;
    private final ShoppingLayout shoppingLayout = new ShoppingLayout(ROUTE_NAME);
    private ProductDTO productDTO;
    private BeforeEvent beforeEvent;
    private Long productId;


    public AbstractProductView(ProductViewService productViewService, ProductSearchService productSearchService,
                               UserRepository userRepository, ProductService productService, ProductSimilarOffersService productSimilarOffersService, ProductRepository productRepository, ProductBasedOnDateAttributesService productDateAttService, BervanLogger log) {
        super();
        this.userRepository = userRepository;
        this.productService = productService;
        this.productSimilarOffersService = productSimilarOffersService;
        this.productRepository = productRepository;
        this.productDateAttService = productDateAttService;
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;
        this.log = log;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long productId) {
        this.beforeEvent = beforeEvent;
        this.productId = productId;
        buildView();
    }

    private void buildView() {
        this.add(shoppingLayout);

        String backLink = buildBackLink(beforeEvent);

        SearchApiResponse byId = productViewService.findById(productId, Pageable.ofSize(1));
        productDTO = (ProductDTO) byId.getItems().iterator().next();

        /// /////////////////////
        VerticalLayout productsLayout = new VerticalLayout();

        VerticalLayout productCard = new VerticalLayout();
        productCard.setWidth("1200px");
        setProductCardStyle(productCard);


        Anchor backButton = new Anchor(backLink, "← Back to products");
        productCard.add(backButton);

        Image image = getProductImage(productDTO);

        image.setWidth("400px");
        image.setHeight("400px");
        image.getStyle().set("object-fit", "contain");

        productCard.add(image, new H3(productDTO.getName()), new Anchor(productDTO.getOfferLink(), productDTO.getOfferLink()));

        List<PriceDTO> prices = productDTO.getPrices();
        Text priceText = new Text("No price");
        if (prices != null && !prices.isEmpty()) {
            priceText = getLatestPriceText(prices, productDTO);
            productCard.add(priceText);

            productCard.add(new Hr());

            VerticalLayout pricesLayout = new VerticalLayout();
            productCard.add(new H3("Last 10 prices:"));

            for (int i = 1; i < prices.size() && i < 10; i++) {
                Text anotherPriceText = new Text(" Price: " + prices.get(i).getPrice() + " zł (" + prices.get(i).getFormattedDate() + ")");
                Div div = new Div();
                div.setClassName("previous-price");
                div.add(anotherPriceText);
                div.add(new Hr());
                pricesLayout.add(div);
            }

            PriceDTO minPrice = productDTO.getMinPrice();
            PriceDTO maxPrice = productDTO.getMaxPrice();
            BigDecimal avgPrice = productDTO.getAvgPrice();

            VerticalLayout pricesSummary = new VerticalLayout();
            pricesSummary.setClassName("prices-summary");
            pricesSummary.add(new H4("Min: " + minPrice.getPrice() + " zł (" + minPrice.getFormattedDate() + ")"));
            pricesSummary.add(new Hr());
            pricesSummary.add(new H4("Avg: " + avgPrice + " zł"));
            pricesSummary.add(new Hr());
            pricesSummary.add(new H4("Max: " + maxPrice.getPrice() + " zł (" + maxPrice.getFormattedDate() + ")"));

            productCard.add(new HorizontalLayout(pricesLayout, pricesSummary));

            prices.sort(Comparator.comparing(PriceDTO::getDate));
            ProductPriceChart productPriceChart = new ProductPriceChart(
                    prices.stream().map(PriceDTO::getDate).map(e -> new SimpleDateFormat("dd-MM-yyyy").format(e)).toList(),
                    prices.stream().map(PriceDTO::getPrice).map(e -> Double.parseDouble(e.toPlainString()))
                            .toList(),
                    avgPrice.doubleValue()
            );
            productCard.add(productPriceChart);
        } else {
            productCard.add(priceText);
        }

        productsLayout.add(productCard);

        add(productsLayout);

        add(new Hr(), new PricesListView(this, productDateAttService, productService, shoppingLayout, productRepository.findById(productId).get(), userRepository));
        add(new Hr());

        List<Long> similarOffers = productSimilarOffersService.findSimilarOffers(productDTO.getId(), 10);
        add(new H4("Similar offers:"));

        for (Long similarOffer : similarOffers) {
            SearchApiResponse res = productViewService.findById(similarOffer, Pageable.ofSize(1));
            ProductDTO next = (ProductDTO) res.getItems().iterator().next();
            add(new HorizontalLayout(new H4(next.getName())));
        }
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

    public void reload() {
        removeAll();
        buildView();
    }
}
