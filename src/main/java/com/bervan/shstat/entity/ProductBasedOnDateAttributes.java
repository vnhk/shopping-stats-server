package com.bervan.shstat.entity;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "scrapDate"})},
        indexes = {@Index(columnList = "formattedScrapDate"), @Index(columnList = "scrapDate")})
public class ProductBasedOnDateAttributes implements AbstractBaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, optional = false, fetch = FetchType.EAGER)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getFormattedScrapDate() {
        return formattedScrapDate;
    }

    public void setFormattedScrapDate(String formattedScrapDate) {
        this.formattedScrapDate = formattedScrapDate;
    }

    public Date getScrapDate() {
        return scrapDate;
    }

    public void setScrapDate(Date scrapDate) {
        this.scrapDate = scrapDate;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}
