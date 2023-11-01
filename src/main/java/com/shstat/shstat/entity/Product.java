package com.shstat.shstat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
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
    private String price;
    private String shop;
    private String productListName;
    private String productListUrl;
    private String formattedScrapDate;
    private Date scrapDate;
    @ElementCollection
    private Set<String> categories = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<ProductAttribute> attributes = new HashSet<>();
}
