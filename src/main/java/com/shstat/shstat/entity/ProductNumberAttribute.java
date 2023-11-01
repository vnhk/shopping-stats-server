package com.shstat.shstat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductNumberAttribute extends ProductAttribute {
    private Number value;

    public ProductNumberAttribute(String name, Number value) {
        this.name = name;
        this.value = value;
    }

    public ProductNumberAttribute() {

    }
}
