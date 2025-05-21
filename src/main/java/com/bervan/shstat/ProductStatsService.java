package com.bervan.shstat;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductStats;
import com.bervan.shstat.repository.ActualProductsRepository;
import com.bervan.shstat.repository.ProductStatsRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

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

        BigDecimal price = (BigDecimal) ProductService.productPerDateAttributeProperties.stream().filter(e -> e.attr.equals("Price")).findFirst()
                .get().mapper.map(priceObj);
        Long id = mappedProduct.getId();
        Optional<ProductStats> byProductId = productStatsRepository.findByProductId(id);
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(id);
            byProductId = Optional.of(productStats);
        }


        if (price.compareTo(BigDecimal.ZERO) > 0) {
            updateStats(byProductId);

            if (byProductId.get().getOwners().isEmpty()) {
                byProductId.get().addOwner(commonUser);
            }
            productStatsRepository.save(byProductId.get());
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
        //It doesnt make sense to update previous AVG, because if avg is for 1 month it can't be updated after 1 month....
        //to make it work in that way the stat should have start and end date fex:
        //- 2 month avg: start [01.01) - end (01.03) and price should be updated only in this range if after then create new avg for next 2 month
        //but it also has low sense.... beacuse avg in that case will be for 1 day... so the best idea is to calculate it EVERY TIME.....
//        if (avg == null || avg.equals(BigDecimal.ZERO)) {
        return createAvgForXMonth(productId, offset);
//        } else {
//            Long amountOfPrices = productStatsRepository.countAllPricesForXMonths(offset, productId); //previous avg was created for amountOfPrices - 1
//            avg = avg.add(price).divide(BigDecimal.valueOf(amountOfPrices), 2, RoundingMode.HALF_UP);
//            return avg;
//        }
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
