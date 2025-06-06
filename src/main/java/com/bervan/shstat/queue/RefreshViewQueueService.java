package com.bervan.shstat.queue;

import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RefreshViewQueueService {
    private final ProductService productService;
    private final ProductRepository productRepository;
    public static final String historicalLowPrices = "HISTORICAL_LOW_PRICES";
    public static final String lowerThanHistoricalLowPrices = "LOWER_THAN_HISTORICAL_LOW_PRICES";
    public static final String lowerThanAvgForLastMonth = "LOWER_THAN_AVG_FOR_LAST_MONTH";
    public static final String lowerThanAvgForLastXMonths = "LOWER_THAN_AVG_FOR_LAST_X_MONTHS";
    public static final List<String> views =
            Arrays.asList(
                    historicalLowPrices,
                    lowerThanHistoricalLowPrices,
                    lowerThanAvgForLastMonth,
                    lowerThanAvgForLastXMonths
            );

    public RefreshViewQueueService(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "0 5 6,12,18 * * *")
    public void refreshViewsScheduled() {
        try {
            refreshViews();
        } catch (Exception e) {
            log.error("RefreshingViews: FAILED!", e);
        }
    }

    private void refreshViews() {
        process(historicalLowPrices);
        process(lowerThanAvgForLastXMonths);
    }

    public void process(String viewName) {
        log.info("Refreshing {} view started... ", viewName);

        switch (viewName) {
            case historicalLowPrices:
                productRepository.refreshHistoricalLowPricesTable();
//                break;
//            case lowerThanHistoricalLowPrices:
//                productRepository.refreshLowerPricesThanHistoricalLowTable();
//                break;
//            case lowerThanAvgForLastMonth:
//                productRepository.refreshLowerThanAVGForLastMonth();
//                break;
            case lowerThanAvgForLastXMonths:
                productService.createLowerThanAVGForLastXMonths();
                break;
        }
        log.info("Refreshing {} view completed... ", viewName);
    }
}
