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
public class ProductTextAttribute extends ProductAttribute {
    private String value;

    public ProductTextAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ProductTextAttribute() {

    }
}
