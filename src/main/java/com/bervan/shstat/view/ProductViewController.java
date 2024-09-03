package com.bervan.shstat.view;

import com.bervan.shstat.response.ApiResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping(path = "/summary-view/products")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<ApiResponse> findProduct(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) UUID id,
                                                   Pageable pageable) {
        if (id == null && (name == null || "".equals(name))) {
            throw new RuntimeException("Id or product name is required!");
        }

        if (id != null) {
            return ResponseEntity.ok(productViewService.findById(id, pageable));
        } else {
            return ResponseEntity.ok(productViewService.findProductContainingName(name, pageable));
        }
    }

    @GetMapping(path = "/historical-low")
    public ResponseEntity<ApiResponse> findHistoricalLowPriceProducts(Pageable pageable,
                                                                      @RequestParam(required = false) String category,
                                                                      @RequestParam(required = false) String name,
                                                                      @RequestParam(required = false) String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }
        return ResponseEntity.ok(discountsViewService.findHistoricalLowPriceProducts(pageable, category, shop, name));
    }

    @GetMapping(path = "/historical-low-discount")
    @Deprecated
    public ResponseEntity<ApiResponse> findXPercentLowerPriceThanHistoricalLow(Pageable pageable,
                                                                               @RequestParam String discountMin,
                                                                               @RequestParam String discountMax,
                                                                               @RequestParam(required = false) Integer prevPriceMin,
                                                                               @RequestParam(required = false) Integer prevPriceMax,
                                                                               @RequestParam(required = false) String category,
                                                                               @RequestParam(required = false) String name,
                                                                               @RequestParam(required = false) boolean onlyActualOffers,
                                                                               @RequestParam(required = false) String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }

        if (discountMin.endsWith("%") && discountMax.endsWith("%")) {
            String numberMin = discountMin.split("%")[0];
            String numberMax = discountMax.split("%")[0];
            return ResponseEntity.ok(discountsViewService.findXPercentLowerPriceThanHistoricalLow(pageable, Double.parseDouble(numberMin),
                    Double.parseDouble(numberMax), category, shop, onlyActualOffers, name, prevPriceMin, prevPriceMax));
        }

        return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage"))
                , HttpStatus.BAD_REQUEST);
    }

    @GetMapping(path = "/discounts-compared-to-avg-in-months")
    public ResponseEntity<ApiResponse> findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable,
                                                                                       @RequestParam String discountMin,
                                                                                       @RequestParam String discountMax,
                                                                                       @RequestParam Integer months,
                                                                                       @RequestParam(required = false) Integer prevPriceMin,
                                                                                       @RequestParam(required = false) Integer prevPriceMax,
                                                                                       @RequestParam(required = false) String name,
                                                                                       @RequestParam(required = false) String category,
                                                                                       @RequestParam(required = false) String shop) {
        if (category != null && StringUtils.isBlank(category.trim())) {
            category = null;
        }

        if (discountMin.endsWith("%") && discountMax.endsWith("%")) {
            String numberMin = discountMin.split("%")[0];
            String numberMax = discountMax.split("%")[0];
            return ResponseEntity.ok(discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, Double.parseDouble(numberMin),
                    Double.parseDouble(numberMax), months, category, shop, name, prevPriceMin, prevPriceMax));
        } else {
            return new ResponseEntity<>(new ApiResponse(Collections.singletonList("The discount should be a percentage.\nThe months must be positive."))
                    , HttpStatus.BAD_REQUEST);
        }
    }
}
