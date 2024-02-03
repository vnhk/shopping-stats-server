package com.shstat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"productId"})})
public class ProductStats {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotNull
    private Long productId;
    private BigDecimal avgWholeHistory;
    private BigDecimal avg1Month;
    private BigDecimal avg2Month;
    private BigDecimal avg3Month;
    private BigDecimal avg6Month;
    private BigDecimal avg12Month;
    private BigDecimal historicalLow;
}
