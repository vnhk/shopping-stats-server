package com.bervan.shstat.entity.scrap;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProductConfig extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String url;
    private Integer minPrice;
    private Integer maxPrice;
    private LocalTime scrapTime;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_config_categories", joinColumns = @JoinColumn(name = "product_config_id"))
    @Column(name = "category")
    private List<String> categories;
    @ManyToOne
    @JoinColumn(name = "shop_config_id")
    private ShopConfig shop;

    private Boolean deleted = false;

    @Override
    public Boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getTableFilterableColumnValue() {
        return name;
    }

    public String getName() {
        return name;
    }

    public ShopConfig getShop() {
        return shop;
    }

    public void setShop(ShopConfig shop) {
        this.shop = shop;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalTime getScrapTime() {
        return scrapTime;
    }

    public void setScrapTime(LocalTime scrapTime) {
        this.scrapTime = scrapTime;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }
}
