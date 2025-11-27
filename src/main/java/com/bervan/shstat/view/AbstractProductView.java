package com.bervan.shstat.view;

import com.bervan.common.component.BervanButton;
import com.bervan.common.component.BervanButtonStyle;
import com.bervan.common.component.BervanTextField;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.ProductBasedOnDateAttributesService;
import com.bervan.shstat.service.ProductSearchService;
import com.bervan.shstat.service.ProductService;
import com.bervan.shstat.tokens.ProductSimilarOffersService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.data.domain.PageImpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractProductView extends BaseProductPage implements HasUrlParameter<Long> {
    public static final String ROUTE_NAME = "/shopping/product";
    private final ProductViewService productViewService;
    private final ProductSearchService productSearchService;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductSimilarOffersService productSimilarOffersService;
    private final ProductRepository productRepository;
    private final ProductBasedOnDateAttributesService productDateAttService;
    
    private final ShoppingLayout shoppingLayout = new ShoppingLayout(ROUTE_NAME);
    private final BervanViewConfig bervanViewConfig;
    private ProductDTO productDTO;
    private BeforeEvent beforeEvent;
    private Long productId;
    private VerticalLayout productsLayout;


    public AbstractProductView(ProductViewService productViewService, ProductSearchService productSearchService,
                               UserRepository userRepository, ProductService productService, ProductSimilarOffersService productSimilarOffersService, ProductRepository productRepository, ProductBasedOnDateAttributesService productDateAttService, BervanViewConfig bervanViewConfig) {
        super();
        this.userRepository = userRepository;
        this.productService = productService;
        this.productSimilarOffersService = productSimilarOffersService;
        this.productRepository = productRepository;
        this.productDateAttService = productDateAttService;
        this.productViewService = productViewService;
        this.productSearchService = productSearchService;

        this.bervanViewConfig = bervanViewConfig;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long productId) {
        this.beforeEvent = beforeEvent;
        this.productId = productId;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        buildView();
    }

    private void buildView() {
        this.setWidthFull();
        removeAll();
        this.add(shoppingLayout);

        productsLayout = new VerticalLayout();
        add(productsLayout);
        Div priceListViewContainer = new Div();
        priceListViewContainer.setWidthFull();
        add(new Hr(), priceListViewContainer, new Hr());

        // --- 🩶 Skeleton placeholder before async loads ---
        VerticalLayout skeletonLayout = getProductSkeletonLayout();
        productsLayout.add(skeletonLayout);

        // --------------------------------------------------
        // --- 🩶 Skeleton for Similar Offers ---
        Div similarOffersContainer = new Div();
        similarOffersContainer.setWidthFull();
        similarOffersContainer.add(new H3("Similar offers:"));

        HorizontalLayout skeletonOffers = new HorizontalLayout();
        skeletonOffers.setSpacing(true);

        for (int i = 0; i < 3; i++) {
            VerticalLayout offerSkeleton = getSimilarOfferSkeleton();
            skeletonOffers.add(offerSkeleton);
        }

        similarOffersContainer.add(skeletonOffers);
        add(similarOffersContainer);
        // ---------------------------------------

        getUI().ifPresent(UI -> UI.access(() -> {
            runAsync(req -> productViewService.findById(productId), productId)
                    .thenAccept(response -> UI.access(() -> {
                        productsLayout.removeAll(); // remove skeleton
                        ProductDTO dto = (ProductDTO) response.getItems().iterator().next();
                        productDTO = dto;
                        buildViewFromProduct();

                        runAsync(req -> productRepository.findById(productId), productId)
                                .thenAccept(res -> UI.access(() -> {
                                    priceListViewContainer.add(
                                            new PricesListView(AbstractProductView.this, productDateAttService, productService, shoppingLayout, res.get(), productViewService, userRepository, bervanViewConfig)
                                    );
                                }));

                        runAsync(req -> productSimilarOffersService.findSimilarOffers(productDTO.getId(), 15), productId)
                                .thenAccept(res -> UI.access(() -> {
                                    //  Remove skeleton before adding real offers
                                    similarOffersContainer.removeAll();
                                    similarOffersContainer.add(new H3("Similar offers:"));

                                    List<ProductDTO> similarOffersProducts = new ArrayList<>();
                                    for (Long similarOffer : res) {
                                        SearchApiResponse apiRes = productViewService.findById(similarOffer);
                                        ProductDTO next = (ProductDTO) apiRes.getItems().iterator().next();
                                        similarOffersProducts.add(next);
                                    }

                                    similarOffersContainer.add(createScrollingLayout(similarOffersProducts));
                                }));
                    }));
        }));
    }

    private VerticalLayout getProductSkeletonLayout() {
        VerticalLayout skeletonLayout = new VerticalLayout();
        skeletonLayout.setWidth("1200px");

        Div imageSkeleton = new Div();
        imageSkeleton.addClassName("skeleton");
        imageSkeleton.setWidth("400px");
        imageSkeleton.setHeight("400px");

        Div titleSkeleton = new Div();
        titleSkeleton.addClassName("skeleton");
        titleSkeleton.setWidth("60%");
        titleSkeleton.setHeight("25px");

        Div priceSkeleton = new Div();
        priceSkeleton.addClassName("skeleton");
        priceSkeleton.setWidth("100px");
        priceSkeleton.setHeight("20px");

        skeletonLayout.add(imageSkeleton, titleSkeleton, priceSkeleton);
        return skeletonLayout;
    }

    private VerticalLayout getSimilarOfferSkeleton() {
        VerticalLayout offerSkeleton = new VerticalLayout();
        offerSkeleton.setWidth("300px");
        offerSkeleton.addClassName("skeleton-offer");

        Div img = new Div();
        img.addClassName("skeleton");
        img.setWidth("300px");
        img.setHeight("300px");

        Div title = new Div();
        title.addClassName("skeleton");
        title.setWidth("80%");
        title.setHeight("20px");

        Div price = new Div();
        price.addClassName("skeleton");
        price.setWidth("60px");
        price.setHeight("18px");

        offerSkeleton.add(img, title, price);
        return offerSkeleton;
    }

    private void buildViewFromProduct() {
        String backLink = buildBackLink(beforeEvent);

        VerticalLayout productCard = new VerticalLayout();

        Button editButton = new Button("✎");
        editButton.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("right", "10px")
                .set("z-index", "10");

        editButton.addClickListener(e -> openEditDialog(productDTO));
        productCard.getStyle().set("position", "relative");
        productCard.add(editButton);

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
    }

    private VerticalLayout createScrollableSection(String title, HorizontalLayout contentLayout) {
        VerticalLayout section = new VerticalLayout();
        section.add(new H3(title));
        section.setWidth("95vw");

        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setAlignItems(Alignment.CENTER);

        Button leftArrow = new Button("<");
        leftArrow.addClassName("option-button");
        leftArrow.addClickListener(event -> contentLayout.getElement().executeJs("this.scrollBy({left: -345, behavior: 'smooth'})"));

        Button rightArrow = new Button(">");
        rightArrow.addClassName("option-button");
        rightArrow.addClickListener(event -> contentLayout.getElement().executeJs("this.scrollBy({left: 345, behavior: 'smooth'})"));

        container.add(leftArrow, contentLayout, rightArrow);
        container.setFlexGrow(1, contentLayout);

        section.add(container);
        return section;
    }

    private HorizontalLayout createScrollingLayout(List<ProductDTO> products) {
        HorizontalLayout scrollingLayout = new HorizontalLayout();
        scrollingLayout.getStyle()
                .set("overflow-x", "hidden")
                .set("white-space", "nowrap")
                .set("padding", "10px");

        for (ProductDTO product : products) {
            VerticalLayout tile = getTile();
            setProductCardStyle(tile);

            Image image = getProductImage(product);

            image.setWidth("300px");
            image.setHeight("300px");
            image.getStyle().set("object-fit", "contain");

            String link = beforeEvent.getLocation().getPathWithQueryParameters();
            link = link.replaceAll("(/product/)\\d+", "$1" + product.getId());

            Anchor nameText = new Anchor(link, product.getName());

            List<PriceDTO> prices = product.getPrices();
            Text priceText = new Text("No price");
            if (prices != null && !prices.isEmpty()) {
                priceText = getLatestPriceText(prices, product);
            }

            tile.add(image, new Hr(), nameText, new Hr(), priceText);
            scrollingLayout.add(tile);
        }

        return scrollingLayout;
    }

    private VerticalLayout getTile() {
        VerticalLayout tile = new VerticalLayout();
        tile.addClassName("offer-tile");
        tile.getStyle()
                .set("margin", "10px")
                .set("cursor", "pointer")
                .set("display", "inline-block")
                .set("min-width", "320px")
                .set("width", "400px")
                .set("height", "540px")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0px 4px 10px rgba(0, 0, 0, 0.1)");
        return tile;
    }

    private String buildBackLink(BeforeEvent beforeEvent) {
        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        String source = (String) getParams(queryParameters, "source");

        Map<String, Object> params = queryParameters.getParameters()
                .entrySet()
                .stream()
                .filter(e -> !"source".equals(e.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> getParams(queryParameters, e.getKey())
                ));

        String paramString = params.entrySet()
                .stream()
                .flatMap(e -> {
                    String key = e.getKey();
                    Object value = e.getValue();

                    if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) value;
                        return list.stream()
                                .map(item -> key + "=" + URLEncoder.encode(String.valueOf(item), StandardCharsets.UTF_8));
                    } else if (value != null) {
                        return Stream.of(key + "=" + URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.joining("&"));
        return source + "?" + paramString;
    }

    public void reload() {
        removeAll();
        buildView();
    }

    private void openEditDialog(ProductDTO productDTO) {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");

        VerticalLayout layout = new VerticalLayout();

        BervanTextField nameField = new BervanTextField("Product Name");
        nameField.setWidth("800px");
        nameField.setValue(productDTO.getName());

        BervanTextField offerLinkField = new BervanTextField("Offer URL");
        offerLinkField.setWidth("800px");
        offerLinkField.setValue(productDTO.getOfferLink());

        BervanTextField imageUrlField = new BervanTextField("Image URL");
        imageUrlField.setWidth("800px");
        imageUrlField.setValue(productDTO.getImgSrc());

        BervanButton saveBtn = new BervanButton("Save", e -> {
            String name = nameField.getValue();
            String link = offerLinkField.getValue();
            String imageInput = imageUrlField.getValue();

            String finalImage;
            try {
                if (imageInput.startsWith("http")) {
                    finalImage = toBase64(imageInput);
                } else {
                    finalImage = imageInput;
                }

                saveChanges(productDTO.getId(), name, link, finalImage);
                showSuccessNotification("Changes saved");
                dialog.close();
                reload();
            } catch (Exception ex) {
                showErrorNotification("Error: " + ex.getMessage());
            }
        }, BervanButtonStyle.PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        layout.add(nameField, offerLinkField, imageUrlField, new HorizontalLayout(saveBtn, cancelBtn));
        dialog.add(layout);
        dialog.open();
    }

    private void saveChanges(Long id, String name, String link, String finalImage) {
        Product update = productService.update(id, name, link, finalImage);
        productViewService.updateCacheWithProductsById(new PageImpl<>(List.of(update)));
        reload();
    }

    private String toBase64(String imageUrl) throws Exception {
        try (InputStream in = new URL(imageUrl).openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        }
    }
}
