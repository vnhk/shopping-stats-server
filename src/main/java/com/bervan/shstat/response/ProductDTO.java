package com.bervan.shstat.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class ProductDTO {
    private Long id;
    private String name;
    private String shop;
    private String offerLink;
    private String imgSrc;
    private Set<String> categories;
    private PriceDTO minPrice;
    private BigDecimal avgPrice;
    private PriceDTO maxPrice;
    private Double discount;
    private List<PriceDTO> prices;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getOfferLink() {
        return offerLink;
    }

    public void setOfferLink(String offerLink) {
        this.offerLink = offerLink;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public PriceDTO getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(PriceDTO minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public PriceDTO getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(PriceDTO maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public List<PriceDTO> getPrices() {
        return prices;
    }

    public void setPrices(List<PriceDTO> prices) {
        this.prices = prices;
    }
}
