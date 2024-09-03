package com.bervan.shstat.entity;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "shop", "productListName", "productListUrl"})},
        indexes = {@Index(columnList = "shop"), @Index(columnList = "name")})
public class Product implements AbstractBaseEntity<UUID> {
    @Id
    @GeneratedValue
    private UUID id;
    @NotNull
    @Size(min = 3, max = 300)
    private String name;
    @NotNull
    @Size(min = 3, max = 150)
    private String shop;
    @NotNull
    private String productListName;
    private String imgSrc;
    @NotNull
    private String productListUrl;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> categories = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "product")
    private Set<ProductAttribute> attributes = new HashSet<>();
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "product", fetch = FetchType.EAGER)
    private Set<ProductBasedOnDateAttributes> productBasedOnDateAttributes = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public String getProductListName() {
        return productListName;
    }

    public void setProductListName(String productListName) {
        this.productListName = productListName;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getProductListUrl() {
        return productListUrl;
    }

    public void setProductListUrl(String productListUrl) {
        this.productListUrl = productListUrl;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<ProductAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ProductAttribute> attributes) {
        this.attributes = attributes;
    }

    public Set<ProductBasedOnDateAttributes> getProductBasedOnDateAttributes() {
        return productBasedOnDateAttributes;
    }

    public void setProductBasedOnDateAttributes(Set<ProductBasedOnDateAttributes> productBasedOnDateAttributes) {
        this.productBasedOnDateAttributes = productBasedOnDateAttributes;
    }

    public void addAttribute(ProductAttribute productAttribute) {
        this.attributes.add(productAttribute);
        productAttribute.setProduct(this);
    }

    public void addAttribute(ProductBasedOnDateAttributes perDateAttributes) {
        this.productBasedOnDateAttributes.add(perDateAttributes);
        perDateAttributes.setProduct(this);
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
