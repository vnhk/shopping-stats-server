package com.bervan.shstat;

import com.bervan.shstat.repository.ActualProductsRepository;
import com.bervan.shstat.repository.ProductStatsRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductStats;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductStatsService {
    private final ProductStatsRepository productStatsRepository;
    private final ActualProductsRepository actualProductsRepository;

    public ProductStatsService(ProductStatsRepository productStatsRepository, ActualProductsRepository actualProductsRepository) {
        this.productStatsRepository = productStatsRepository;
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateProductStats(Product mappedProduct, Object priceObj) {
        if (actualProductsRepository.findByProductId(mappedProduct.getId()).isEmpty()) {
            return;
        }

        BigDecimal price = (BigDecimal) ProductService.productPerDateAttributeProperties.stream().filter(e -> e.attr.equals("Price")).findFirst()
                .get().mapper.map(priceObj);
        UUID id = mappedProduct.getId();
        Optional<ProductStats> byProductId = productStatsRepository.findByProductId(id);
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(id);
            byProductId = Optional.of(productStats);
        }

        if (price.compareTo(BigDecimal.ZERO) > 0) {
            updateHistoricalLow(byProductId.get(), price);
            updateAvgWholeHistory(byProductId.get(), price);
            updateAvgLast1Month(byProductId.get(), price);
            updateAvgLast2Month(byProductId.get(), price);
            updateAvgLast3Month(byProductId.get(), price);
            updateAvgLast6Month(byProductId.get(), price);
            updateAvgLast12Month(byProductId.get(), price);

            productStatsRepository.save(byProductId.get());
        }
    }

    private void updateHistoricalLow(ProductStats productStats, BigDecimal price) {
        BigDecimal historicalLow = productStats.getHistoricalLow();
        productStats.setHistoricalLow(findHistoricalLowForMonths(productStats.getProductId(), historicalLow, 12000, price));
    }

    private void updateAvgWholeHistory(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvgWholeHistory();
        productStats.setAvgWholeHistory(calculateAvgForMonths(productStats.getProductId(), avg, 12000, price));
    }

    private void updateAvgLast12Month(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvg12Month();
        productStats.setAvg12Month(calculateAvgForMonths(productStats.getProductId(), avg, 12, price));
    }

    private void updateAvgLast6Month(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvg6Month();
        productStats.setAvg6Month(calculateAvgForMonths(productStats.getProductId(), avg, 6, price));
    }

    private void updateAvgLast3Month(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvg3Month();
        productStats.setAvg3Month(calculateAvgForMonths(productStats.getProductId(), avg, 3, price));
    }

    private void updateAvgLast2Month(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvg2Month();
        productStats.setAvg2Month(calculateAvgForMonths(productStats.getProductId(), avg, 2, price));
    }

    private void updateAvgLast1Month(ProductStats productStats, BigDecimal price) {
        BigDecimal avg = productStats.getAvg1Month();
        productStats.setAvg1Month(calculateAvgForMonths(productStats.getProductId(), avg, 1, price));
    }

    private BigDecimal findHistoricalLowForMonths(UUID productId, BigDecimal historicalLow, int offset, BigDecimal price) {
//        if (historicalLow == null || historicalLow.equals(BigDecimal.ZERO)) {
        return createHistoricalLowForXMonth(productId, offset);
//        } else {
//            if (price.compareTo(historicalLow) < 0) {
//                return price;
//            } else {
//                return historicalLow;
//            }
//        }
    }

    private BigDecimal calculateAvgForMonths(@NotNull UUID productId, BigDecimal avg, int offset, BigDecimal price) {
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

    private BigDecimal createHistoricalLowForXMonth(UUID productId, int offset) {
        return productStatsRepository.findHistoricalLowForXMonths(offset, productId);
    }

    private BigDecimal createAvgForXMonth(UUID productId, int offset) {
        return productStatsRepository.calculateAvgForXMonths(offset, productId);
    }
}
