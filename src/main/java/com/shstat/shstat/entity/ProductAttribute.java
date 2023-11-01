package com.shstat.shstat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductAttribute {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Product product;
    private String name;
    private String value;
}
