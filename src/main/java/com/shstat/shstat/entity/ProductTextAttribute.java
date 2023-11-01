package com.shstat.shstat.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductTextAttribute extends ProductAttribute {
    private String value;

    public ProductTextAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ProductTextAttribute() {

    }
}
