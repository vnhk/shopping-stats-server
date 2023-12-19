package com.shstat.favorites;

import com.shstat.entity.ProductEmailNotification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"listName"})})
public class FavoritesList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Size(min = 3, max = 300)
    private String listName;
    @ManyToOne
    @JoinColumn(name = "product_email_notification_id")
    private ProductEmailNotification productEmailNotification;
    @OneToMany(mappedBy = "favoritesList")
    private Set<FavoritesRule> favoritesRules = new HashSet<>();
}
