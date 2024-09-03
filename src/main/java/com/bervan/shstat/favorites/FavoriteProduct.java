package com.bervan.shstat.favorites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
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

    public FavoriteProduct(Long id, Long productId, String productName, String shop, String category, BigDecimal price, String listName, BigDecimal avgPrice, String imgSrc, Date scrapDate, String offerUrl, BigDecimal discountInPercent) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.shop = shop;
        this.category = category;
        this.price = price;
        this.listName = listName;
        this.avgPrice = avgPrice;
        this.imgSrc = imgSrc;
        this.scrapDate = scrapDate;
        this.offerUrl = offerUrl;
        this.discountInPercent = discountInPercent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public Date getScrapDate() {
        return scrapDate;
    }

    public void setScrapDate(Date scrapDate) {
        this.scrapDate = scrapDate;
    }

    public String getOfferUrl() {
        return offerUrl;
    }

    public void setOfferUrl(String offerUrl) {
        this.offerUrl = offerUrl;
    }

    public BigDecimal getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(BigDecimal discountInPercent) {
        this.discountInPercent = discountInPercent;
    }

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
