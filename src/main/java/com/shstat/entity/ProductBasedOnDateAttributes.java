package com.shstat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    @NotNull
    private BigDecimal price;
    @NotNull
    @Size(min = 3, max = 300)
    private String formattedScrapDate;
    @NotNull
    private Date scrapDate;

    public ProductBasedOnDateAttributes() {

    }

    public ProductBasedOnDateAttributes(@NotNull BigDecimal price, @NotNull Date scrapDate, @NotNull String formattedScrapDate) {
        this.price = price;
        this.scrapDate = scrapDate;
        this.formattedScrapDate = formattedScrapDate;
    }
}
