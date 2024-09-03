package com.bervan.shstat.entity;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"productId"})})
public class ProductStats implements AbstractBaseEntity<UUID> {
    @Id
    @GeneratedValue
    private UUID id;
    @NotNull
    private UUID productId;
    private BigDecimal avgWholeHistory;
    private BigDecimal avg1Month;
    private BigDecimal avg2Month;
    private BigDecimal avg3Month;
    private BigDecimal avg6Month;
    private BigDecimal avg12Month;
    private BigDecimal historicalLow;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getAvgWholeHistory() {
        return avgWholeHistory;
    }

    public void setAvgWholeHistory(BigDecimal avgWholeHistory) {
        this.avgWholeHistory = avgWholeHistory;
    }

    public BigDecimal getAvg1Month() {
        return avg1Month;
    }

    public void setAvg1Month(BigDecimal avg1Month) {
        this.avg1Month = avg1Month;
    }

    public BigDecimal getAvg2Month() {
        return avg2Month;
    }

    public void setAvg2Month(BigDecimal avg2Month) {
        this.avg2Month = avg2Month;
    }

    public BigDecimal getAvg3Month() {
        return avg3Month;
    }

    public void setAvg3Month(BigDecimal avg3Month) {
        this.avg3Month = avg3Month;
    }

    public BigDecimal getAvg6Month() {
        return avg6Month;
    }

    public void setAvg6Month(BigDecimal avg6Month) {
        this.avg6Month = avg6Month;
    }

    public BigDecimal getAvg12Month() {
        return avg12Month;
    }

    public void setAvg12Month(BigDecimal avg12Month) {
        this.avg12Month = avg12Month;
    }

    public BigDecimal getHistoricalLow() {
        return historicalLow;
    }

    public void setHistoricalLow(BigDecimal historicalLow) {
        this.historicalLow = historicalLow;
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }
}