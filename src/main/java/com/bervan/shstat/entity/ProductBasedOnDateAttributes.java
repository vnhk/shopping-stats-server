package com.bervan.shstat.entity;

import com.bervan.common.model.BervanBaseEntity;
import com.bervan.common.model.PersistableTableData;
import com.bervan.common.model.VaadinTableColumn;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "scrapDate"})},
        indexes = {@Index(columnList = "formattedScrapDate"), @Index(columnList = "scrapDate")})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public class ProductBasedOnDateAttributes extends BervanBaseEntity<Long> implements PersistableTableData<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;
    @NotNull
    @VaadinTableColumn(internalName = "price", displayName = "Price")
    private BigDecimal price;
    @NotNull
    @Size(min = 3, max = 300)
    @VaadinTableColumn(internalName = "formattedScrapDate", displayName = "Formatted Scrap Date End")
    private String formattedScrapDate;
    @NotNull
    @VaadinTableColumn(internalName = "scrapDate", displayName = "Scrap Date")
    private Date scrapDate;
    @VaadinTableColumn(internalName = "scrapDateEnd", displayName = "Scrap Date End")
    private Date scrapDateEnd;
    @VaadinTableColumn(internalName = "formattedScrapDateEnd", displayName = "Formatted Scrap Date End")
    private String formattedScrapDateEnd;
    private Boolean deleted = false;

    public ProductBasedOnDateAttributes() {

    }

    @Override
    public Boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(Boolean value) {
        throw new RuntimeException("ProductBasedOnDateAttributes does not use 'deleted'!");
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }

    @Override
    public String getTableFilterableColumnValue() {
        return formattedScrapDate;
    }
}
