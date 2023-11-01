package com.shstat.shstat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String url;
    private String href;
    private Integer price;
    @OneToMany
    private Set<ProductAttribute> attributes = new HashSet<>();
}
