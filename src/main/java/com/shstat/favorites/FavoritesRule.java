package com.shstat.favorites;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
    private String category;
    private boolean onlyActive;
    @ManyToOne
    @JoinColumn(name = "favorites_list_id")
    private FavoritesList favoritesList;
}
