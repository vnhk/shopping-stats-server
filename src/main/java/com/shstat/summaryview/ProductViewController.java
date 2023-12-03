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
public class ProductViewController {
    private final ProductViewService productViewService;
    private final DiscountsViewService discountsViewService;

    public ProductViewController(ProductViewService productViewService,
                                 DiscountsViewService discountsViewService) {
        this.productViewService = productViewService;
        this.discountsViewService = discountsViewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getProductList(@RequestParam String category,
                                                      @RequestParam(required = false) String shop,
                                                      Pageable pageable) {
        return ResponseEntity.ok(productViewService.findProductsByCategory(category, shop, pageable));
    }

    @GetMapping(path = "/product")
    public ResponseEntity<ApiResponse> findProductContainingName(@RequestParam String name) {
        return ResponseEntity.ok(productViewService.findProductContainingName(name));
    }

    @GetMapping(path = "/historical-low")
    public ResponseEntity<ApiResponse> findHistoricalLowPriceProducts(Pageable pageable) {
        return ResponseEntity.ok(discountsViewService.findHistoricalLowPriceProducts(pageable));
    }

    @GetMapping(path = "/historical-low-discount")
    public ResponseEntity<ApiResponse> findXPercentLowerPriceThanHistoricalLow(Pageable pageable, @RequestParam String discount) {
        if (discount.endsWith("%")) {
            String number = discount.split("%")[0];
            return ResponseEntity.ok(discountsViewService.findXPercentLowerPriceThanHistoricalLow(pageable, (100 - Double.parseDouble(number)) * 0.01));
        }

        return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage"))
                , HttpStatus.BAD_REQUEST);
    }

    @GetMapping(path = "/discounts-compared-to-avg-in-months")
    public ResponseEntity<ApiResponse> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                                       @RequestParam String discount,
                                                                                       @RequestParam Integer months) {
        if (discount.endsWith("%") && months > 0) {
            String number = discount.split("%")[0];
            return ResponseEntity.ok(discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, (100 - Double.parseDouble(number)) * 0.01, months));
        } else {
            return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage.\nThe months must be positive."))
                    , HttpStatus.BAD_REQUEST);
        }
    }
}
