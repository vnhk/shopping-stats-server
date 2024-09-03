package com.bervan.shstat.entity;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity
@Table(indexes = {@Index(columnList = "name")})
public abstract class ProductAttribute implements AbstractBaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    @NotNull
    @Size(min = 3, max = 300)
    protected String name;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    protected Product product;

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
