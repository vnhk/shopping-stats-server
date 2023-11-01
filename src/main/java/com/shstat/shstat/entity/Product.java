package com.shstat.shstat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String url;
    private String href;
    private String price;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<ProductAttribute> attributes = new HashSet<>();
}
