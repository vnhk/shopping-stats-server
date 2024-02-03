package com.shstat.queue;

import com.shstat.ProductService;
import com.shstat.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RefreshViewQueue extends AbstractQueue<RefreshViewQueueParam> {
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

    public RefreshViewQueue(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Override
    protected void process(Serializable object) {
        log.info("Currently no views are refreshed!");

        RefreshViewQueueParam param = (RefreshViewQueueParam) object;
//        log.info("Refreshing product {} view started...", param.getViewName());
//        switch (param.getViewName()) {
//            case historicalLowPrices:
//                productRepository.refreshHistoricalLowPricesTable();
//                break;
//            case lowerThanHistoricalLowPrices:
//                productRepository.refreshLowerPricesThanHistoricalLowTable();
//                break;
//            case lowerThanAvgForLastMonth:
//                productRepository.refreshLowerThanAVGForLastMonth();
//                break;
//            case lowerThanAvgForLastXMonths:
//                productService.lowerThanAVGForLastXMonths();
//                break;
//        }
//        log.info("Refreshing product {} view completed...", param.getViewName());
    }
}
