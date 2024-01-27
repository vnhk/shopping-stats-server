package com.shstat.entity;

import com.shstat.favorites.FavoritesList;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class ProductEmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bodyTemplate;

    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Double discount;

    @OneToMany(mappedBy = "productEmailNotification", fetch = FetchType.EAGER)
    private Set<FavoritesList> favoritesLists = new HashSet<>();
}
