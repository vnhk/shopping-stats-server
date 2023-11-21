package com.shstat.entity;

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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "shop", "productListName", "productListUrl"})})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Size(min = 3, max = 300)
    private String name;
    @NotNull
    @Size(min = 3, max = 150)
    private String shop;
    @NotNull
    private String productListName;
    @NotNull
    private String productListUrl;
    @ElementCollection
    private Set<String> categories = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "product")
    private Set<ProductAttribute> attributes = new HashSet<>();
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "product")
    private Set<ProductBasedOnDateAttributes> productBasedOnDateAttributes = new HashSet<>();

    public void addAttribute(ProductAttribute productAttribute) {
        this.attributes.add(productAttribute);
        productAttribute.setProduct(this);
    }

    public void addAttribute(ProductBasedOnDateAttributes perDateAttributes) {
        this.productBasedOnDateAttributes.add(perDateAttributes);
        perDateAttributes.setProduct(this);
    }
}
