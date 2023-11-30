package com.shstat.summaryview;

import com.shstat.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping(path = "/summary-view/products")
public class SummaryViewController {
    private final SummaryViewService summaryViewService;
    private final HistoricalLowPricesViewService historicalLowPricesViewService;

    public SummaryViewController(SummaryViewService summaryViewService,
                                 HistoricalLowPricesViewService historicalLowPricesViewService) {
        this.summaryViewService = summaryViewService;
        this.historicalLowPricesViewService = historicalLowPricesViewService;
    }

    @GetMapping(path = "/product")
    public ResponseEntity<ApiResponse> findProductContainingName(@RequestParam String name) {
        return ResponseEntity.ok(summaryViewService.findProductContainingName(name));
    }

    @GetMapping(path = "/historical-low")
    public ResponseEntity<ApiResponse> findHistoricalLowPriceProducts(Pageable pageable) {
        return ResponseEntity.ok(historicalLowPricesViewService.findHistoricalLowPriceProducts(pageable));
    }

    @GetMapping(path = "/historical-low-discount")
    public ResponseEntity<ApiResponse> find10PercentLowerPriceThanHistoricalLow(Pageable pageable, @RequestParam String discount) {
        if (discount.endsWith("%")) {
            String number = discount.split("%")[0];
            return ResponseEntity.ok(historicalLowPricesViewService.findXPercentLowerPriceThanHistoricalLow(pageable, (100 - Double.parseDouble(number)) * 0.01));
        }

        return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage"))
                , HttpStatus.BAD_REQUEST);
    }
}
