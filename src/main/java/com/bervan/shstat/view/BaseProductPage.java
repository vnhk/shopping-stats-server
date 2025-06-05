package com.bervan.shstat.view;

import com.bervan.common.AbstractPageView;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.util.List;

public abstract class BaseProductPage extends AbstractPageView {

    protected void setProductCardStyle(VerticalLayout productCard) {
        productCard.getStyle().set("border", "1px solid #ccc");
        productCard.getStyle().set("border-radius", "8px");
        productCard.getStyle().set("padding", "10px");
        productCard.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
        productCard.getStyle().set("background-color", "#fff");
        productCard.getStyle().set("text-align", "center");
    }


    protected Image getProductImage(ProductDTO productDTO) {
        Image image = new Image(productDTO.getImgSrc() == null ? "" : productDTO.getImgSrc(), "No image :(");
        if (productDTO.getImgSrc().startsWith("http") || productDTO.getImgSrc().startsWith("https")) {
            image.setSrc(productDTO.getImgSrc());
        } else {
            image.setSrc("data:image/png;base64," + productDTO.getImgSrc());
        }
        return image;
    }

    protected Text getLatestPriceText(List<PriceDTO> prices, ProductDTO productDTO) {
        StringBuilder priceTextBuilder = getLatestPriceTextWithDiscount(prices, productDTO);
        return new Text(priceTextBuilder.toString());
    }

    private static StringBuilder getLatestPriceTextWithDiscount(List<PriceDTO> prices, ProductDTO productDTO) {
        PriceDTO latest = prices.get(0);
        BigDecimal latestPrice = latest.getPrice();

        BigDecimal avgPrice = productDTO.getAvgPrice();

        StringBuilder priceTextBuilder = new StringBuilder(" Price: " + latestPrice + " zł");

        if (avgPrice != null && latestPrice != null && avgPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = latestPrice.subtract(avgPrice);
            BigDecimal percentage = diff.abs().divide(avgPrice, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (latestPrice.compareTo(avgPrice) > 0) {
                priceTextBuilder.append(" (+" + percentage + "% avg)");
            } else if (latestPrice.compareTo(avgPrice) < 0) {
                priceTextBuilder.append(" (-" + percentage + "% avg)");
            }
        }
        return priceTextBuilder;
    }


}
