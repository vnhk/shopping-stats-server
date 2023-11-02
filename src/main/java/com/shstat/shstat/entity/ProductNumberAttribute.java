package com.shstat.shstat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "name", "value"})})
public class ProductNumberAttribute extends ProductAttribute {
    private Number value;

    public ProductNumberAttribute(String name, Number value) {
        this.name = name;
        this.value = value;
    }

    public ProductNumberAttribute() {

    }
}
