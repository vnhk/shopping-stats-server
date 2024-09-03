package com.bervan.shstat.entity;

import com.bervan.history.model.AbstractBaseEntity;
import com.bervan.shstat.favorites.FavoritesList;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class ProductEmailNotification implements AbstractBaseEntity<UUID> {
    @Id
    @GeneratedValue
    private UUID id;

    private String bodyTemplate;

    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Double discount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
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

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Set<FavoritesList> getFavoritesLists() {
        return favoritesLists;
    }

    public void setFavoritesLists(Set<FavoritesList> favoritesLists) {
        this.favoritesLists = favoritesLists;
    }

    @OneToMany(mappedBy = "productEmailNotification", fetch = FetchType.EAGER)
    private Set<FavoritesList> favoritesLists = new HashSet<>();

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
