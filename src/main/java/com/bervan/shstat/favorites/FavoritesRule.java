package com.bervan.shstat.favorites;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"ruleName"})})
public class FavoritesRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Size(min = 3, max = 300)
    private String ruleName;
    private Long productId;
    private String shop;
    private String productName;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String category;
    private boolean onlyActive;
    private boolean disabled;
    @ManyToOne
    @JoinColumn(name = "favorites_list_id")
    private FavoritesList favoritesList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;
    }

    public BigDecimal getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isOnlyActive() {
        return onlyActive;
    }

    public void setOnlyActive(boolean onlyActive) {
        this.onlyActive = onlyActive;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public FavoritesList getFavoritesList() {
        return favoritesList;
    }

    public void setFavoritesList(FavoritesList favoritesList) {
        this.favoritesList = favoritesList;
    }
}
