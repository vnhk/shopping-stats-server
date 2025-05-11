package com.bervan.shstat.entity.scrap;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ShopConfig extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    @Id
    @GeneratedValue
    private Long id;
    @VaadinTableColumn(displayName = "Shop Name", internalName = "shopName")
    private String shopName;
    @VaadinTableColumn(displayName = "Base Url", internalName = "baseUrl")
    private String baseUrl;
    private Boolean deleted = false;
    @OneToMany(mappedBy = "shop")
    private Set<ProductConfig> products = new HashSet<>();

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
        return shopName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Set<ProductConfig> getProducts() {
        return products;
    }

    public void setProducts(Set<ProductConfig> products) {
        this.products = products;
    }
}
