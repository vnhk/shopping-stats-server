package com.bervan.shstat.entity;

import com.bervan.common.model.BervanBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity
@Table(indexes = {@Index(columnList = "name")})
public abstract class ProductAttribute extends BervanBaseEntity<Long> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute_seq")
    @SequenceGenerator(
            name = "product_attribute_seq",
            sequenceName = "product_attribute_seq",
            allocationSize = 1
    )
    @Id
    protected Long id;
    @NotNull
    @Size(min = 3, max = 300)
    protected String name;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    protected Product product;
    private Boolean deleted = false;


    @Override
    public Boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

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
