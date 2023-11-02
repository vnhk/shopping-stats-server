package com.shstat.shstat.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "name"})})
public class ProductListTextAttribute extends ProductAttribute {
    @ElementCollection
    private Set<String> value;

    public ProductListTextAttribute(String name, Set<String> value) {
        this.name = name;
        this.value = value;
    }

    public ProductListTextAttribute() {

    }
}
