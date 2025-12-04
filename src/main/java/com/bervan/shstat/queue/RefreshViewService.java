package com.bervan.shstat.queue;

import com.bervan.logging.JsonLogger;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.service.ProductService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RefreshViewService {
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
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");
    private final ProductService productService;
    private final ProductRepository productRepository;

    public RefreshViewService(ProductService productService, ProductRepository productRepository) {
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
//        process(historicalLowPrices);
        process(lowerThanAvgForLastXMonths);
    }

    public void process(String viewName) {
        log.info("Refreshing {} view started... ", viewName);
        long start = System.nanoTime();

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
                productService.createBestOffers();
                break;
        }
        long end = System.nanoTime();
        double durationInSeconds = (end - start) / 1_000_000_000.0;

        log.info("Refreshing {} view completed... It took {} ms", viewName, durationInSeconds);
    }
}
