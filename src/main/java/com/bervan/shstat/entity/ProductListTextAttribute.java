package com.bervan.shstat.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "name"})})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProductListTextAttribute extends ProductAttribute {
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> value;

    public Set<String> getValue() {
        return value;
    }

    public void setValue(Set<String> value) {
        this.value = value;
    }

    public ProductListTextAttribute(String name, Set<String> value) {
        this.name = name;
        this.value = value;
    }

    public ProductListTextAttribute() {

    }


    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
