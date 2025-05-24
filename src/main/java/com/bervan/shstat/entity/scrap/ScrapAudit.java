package com.bervan.shstat.entity.scrap;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ScrapAudit extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //We want to do processing once per day, but if product have to be scrapped more than 1 time just add new product config!
    @VaadinTableColumn(displayName = "Date", internalName = "date")
    private LocalDate date;

    @Transient
    @VaadinTableColumn(displayName = "Product", internalName = "product", inSaveForm = false, inEditForm = false)
    private String productDetails;

    @VaadinTableColumn(displayName = "Saved", internalName = "saved", inSaveForm = false, inEditForm = false)
    private long savedProducts = 0;

    @ManyToOne
    @JoinColumn(name = "product_config_id")
    private ProductConfig productConfig;


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
        return date.toString();
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public ProductConfig getProductConfig() {
        return productConfig;
    }

    public void setProductConfig(ProductConfig productConfig) {
        this.productConfig = productConfig;
    }

    public String getProductDetails() {
        StringBuilder builder = new StringBuilder();
        if (productConfig != null) {
            builder.append(productConfig.getName());
            builder.append(" - ");
            builder.append(productConfig.getShop().getShopName());
            builder.append(" (");
            builder.append(productConfig.getScrapTime().getHour());
            builder.append(":00)");
        }
        return builder.toString();
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public long getSavedProducts() {
        return savedProducts;
    }

    public synchronized void addToSavedProducts(int toBeAdded) {
        savedProducts += toBeAdded;
    }
}
