package com.shstat.shstat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "scrapDate"})})
public class ProductBasedOnDateAttributes {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
    private String price;
    private String formattedScrapDate;
    private Date scrapDate;
}
