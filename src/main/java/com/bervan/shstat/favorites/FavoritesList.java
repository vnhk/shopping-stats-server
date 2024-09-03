package com.bervan.shstat.favorites;

import com.bervan.history.model.AbstractBaseEntity;
import com.bervan.shstat.entity.ProductEmailNotification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"listName"})})
public class FavoritesList implements AbstractBaseEntity<UUID> {
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Size(min = 3, max = 300)
    private String listName;
    private boolean disabled;
    @ManyToOne
    @JoinColumn(name = "product_email_notification_id")
    private ProductEmailNotification productEmailNotification;
    @OneToMany(mappedBy = "favoritesList", fetch = FetchType.EAGER)
    private Set<FavoritesRule> favoritesRules = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public ProductEmailNotification getProductEmailNotification() {
        return productEmailNotification;
    }

    public void setProductEmailNotification(ProductEmailNotification productEmailNotification) {
        this.productEmailNotification = productEmailNotification;
    }

    public Set<FavoritesRule> getFavoritesRules() {
        return favoritesRules;
    }

    public void setFavoritesRules(Set<FavoritesRule> favoritesRules) {
        this.favoritesRules = favoritesRules;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
