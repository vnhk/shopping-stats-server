package com.bervan.shstat.entity;

import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.model.PersistableTableOwnedData;
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
public class ProductAlert extends BervanOwnedBaseEntity<Long> implements PersistableTableOwnedData<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer priceMin;
    private Integer priceMax;
    private Integer discountMin;
    private Integer discountMax;
    private String productName;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_alert_categories", joinColumns = @JoinColumn(name = "product_alert_id"))
    @Column(name = "category")
    private List<String> productCategories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_alert_emails", joinColumns = @JoinColumn(name = "product_alert_id"))
    @Column(name = "email")
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
