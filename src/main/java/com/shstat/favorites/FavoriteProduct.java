package com.shstat.favorites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteProduct {
    @Id
    @GeneratedValue
    private Long id;
    private Long productId;
    private String productName;
    private String shop;
    private String category;
    private BigDecimal price;
    private String listName;
    private BigDecimal avgPrice;
    private String imgSrc;
    private Date scrapDate;
    private String offerUrl;
    private BigDecimal discountInPercent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteProduct that = (FavoriteProduct) o;
        return Objects.equals(productId, that.productId)
                && Objects.equals(productName, that.productName)
                && Objects.equals(shop, that.shop)
                && Objects.equals(category, that.category) && Objects.equals(price, that.price)
                && Objects.equals(listName, that.listName) && Objects.equals(avgPrice, that.avgPrice)
                && Objects.equals(imgSrc, that.imgSrc) && Objects.equals(scrapDate, that.scrapDate)
                && Objects.equals(offerUrl, that.offerUrl) && Objects.equals(discountInPercent, that.discountInPercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, shop, category, price, listName, avgPrice, imgSrc, scrapDate, offerUrl, discountInPercent);
    }
}
