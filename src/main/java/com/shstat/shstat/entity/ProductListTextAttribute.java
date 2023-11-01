package com.shstat.shstat.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class ProductListTextAttribute extends ProductAttribute {
    @ElementCollection
    private List<String> value;

    public ProductListTextAttribute(String name, List<String> value) {
        this.name = name;
        this.value = value;
    }

    public ProductListTextAttribute() {

    }
}
