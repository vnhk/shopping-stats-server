package com.bervan.shstat.entity;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinDynamicMultiDropdownBervanColumn;
import com.bervan.common.model.VaadinBervanColumn;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public class ProductAlert extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @VaadinBervanColumn(displayName = "Alert Name", internalName = "alertName")
    private String name;
    @VaadinBervanColumn(displayName = "Price Min", internalName = "priceMin")
    private Integer priceMin;
    @VaadinBervanColumn(displayName = "Price Max", internalName = "priceMax")
    private Integer priceMax;
    @VaadinBervanColumn(displayName = "Discount Min", internalName = "discountMin")
    private Integer discountMin;
    @VaadinBervanColumn(displayName = "Discount Max", internalName = "discountMax")
    private Integer discountMax;
    @VaadinBervanColumn(displayName = "Product Name", internalName = "productName")
    private String productName;
    @VaadinBervanColumn(displayName = "Product Categories", internalName = "productCategories", extension = VaadinDynamicMultiDropdownBervanColumn.class)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_alert_categories", joinColumns = @JoinColumn(name = "product_alert_id"))
    @Column(name = "category")
    private List<String> productCategories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_alert_emails", joinColumns = @JoinColumn(name = "product_alert_id"))
    @Column(name = "email")
    @VaadinBervanColumn(displayName = "Emails", internalName = "emails", extension = VaadinDynamicMultiDropdownBervanColumn.class)
    private List<String> emails = new ArrayList<>();

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
        return id.toString();
    }
}
