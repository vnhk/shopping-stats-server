package com.bervan.shstat.favorites;

import com.bervan.shstat.entity.ProductEmailNotification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"listName"})})
public class FavoritesList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 300)
    private String listName;
    private boolean disabled;
    @ManyToOne
    @JoinColumn(name = "product_email_notification_id")
    private ProductEmailNotification productEmailNotification;
    @OneToMany(mappedBy = "favoritesList", fetch = FetchType.EAGER)
    private Set<FavoritesRule> favoritesRules = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
