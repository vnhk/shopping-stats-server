package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductStats;
import com.bervan.shstat.repository.ActualProductsRepository;
import com.bervan.shstat.repository.ProductStatsRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class ProductStatsService {
    private final ProductStatsRepository productStatsRepository;
    private final ActualProductsRepository actualProductsRepository;

    public ProductStatsService(ProductStatsRepository productStatsRepository, ActualProductsRepository actualProductsRepository) {
        this.productStatsRepository = productStatsRepository;
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateProductStats(Product mappedProduct, Object priceObj, User commonUser) {
        if (actualProductsRepository.findByProductId(mappedProduct.getId()).isEmpty()) {
            return;
        }

        if (mappedProduct.getProductBasedOnDateAttributes() == null || mappedProduct.getProductBasedOnDateAttributes().stream().filter(e -> !e.isDeleted()).count() < 2) {
            log.warn("updateProductStats - No sense to create stats because there is no enough historical data for product: {} id", mappedProduct.getId());
            return;
        }

        BigDecimal lastPrice = (BigDecimal) ProductService.productPerDateAttributeProperties.stream().filter(e -> e.attr.equals("Price")).findFirst()
                .get().mapper.map(priceObj);
        Long id = mappedProduct.getId();
        Optional<ProductStats> byProductId = productStatsRepository.findByProductId(id);
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(id);
            byProductId = Optional.of(productStats);
        }

        if (lastPrice.compareTo(BigDecimal.ZERO) > 0) {
            updateStats(byProductId);

            if (byProductId.get().getOwners().isEmpty()) {
                byProductId.get().addOwner(commonUser);
            }

            synchronized (this) {
                productStatsRepository.save(byProductId.get());
            }
        }
    }

    public void updateStatsAndSave(Optional<ProductStats> byProductId, Long productId) {
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(productId);
            byProductId = Optional.of(productStats);
        }
        updateStats(byProductId);
        productStatsRepository.save(byProductId.get());
    }

    private void updateStats(Optional<ProductStats> byProductId) {
        updateHistoricalLow(byProductId.get());
        updateAvgWholeHistory(byProductId.get());
        updateAvgLast1Month(byProductId.get());
        updateAvgLast2Month(byProductId.get());
        updateAvgLast3Month(byProductId.get());
        updateAvgLast6Month(byProductId.get());
        updateAvgLast12Month(byProductId.get());
    }

    private void updateHistoricalLow(ProductStats productStats) {
        productStats.setHistoricalLow(findHistoricalLowForMonths(productStats.getProductId(), 12000));
    }

    private void updateAvgWholeHistory(ProductStats productStats) {
        productStats.setAvgWholeHistory(calculateAvgForMonths(productStats.getProductId(), 12000));
    }

    private void updateAvgLast12Month(ProductStats productStats) {
        productStats.setAvg12Month(calculateAvgForMonths(productStats.getProductId(), 12));
    }

    private void updateAvgLast6Month(ProductStats productStats) {
        productStats.setAvg6Month(calculateAvgForMonths(productStats.getProductId(), 6));
    }

    private void updateAvgLast3Month(ProductStats productStats) {
        productStats.setAvg3Month(calculateAvgForMonths(productStats.getProductId(), 3));
    }

    private void updateAvgLast2Month(ProductStats productStats) {
        productStats.setAvg2Month(calculateAvgForMonths(productStats.getProductId(), 2));
    }

    private void updateAvgLast1Month(ProductStats productStats) {
        productStats.setAvg1Month(calculateAvgForMonths(productStats.getProductId(), 1));
    }

    private BigDecimal findHistoricalLowForMonths(Long productId, int offset) {
        return createHistoricalLowForXMonth(productId, offset);
    }

    private BigDecimal calculateAvgForMonths(@NotNull Long productId, int offset) {
        return createAvgForXMonth(productId, offset);
    }

    private BigDecimal createHistoricalLowForXMonth(Long productId, int offset) {
        return productStatsRepository.findHistoricalLowForXMonths(offset, productId);
    }

    private BigDecimal createAvgForXMonth(Long productId, int offset) {
        return productStatsRepository.calculateAvgForXMonths(offset, productId);
    }

    public Optional<ProductStats> findByProductId(Long id) {
        return productStatsRepository.findByProductId(id);
    }
}
