package com.shstat.shstat;

import com.shstat.shstat.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;

    public ProductController(ProductService productService, SearchService searchService) {
        this.productService = productService;
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addProducts(@RequestBody List<Map<String, Object>> products) {
        return ResponseEntity.ok(productService.addProducts(products));
    }


    @GetMapping(path = "/basic-search")
    public ResponseEntity<ApiResponse> getLatestProducts(@RequestParam(required = false) String categories,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String shop,
                                                         @RequestParam(required = false) Integer priceMin,
                                                         @RequestParam(required = false) Integer priceMax) {
        return ResponseEntity.ok(searchService.getProducts(categories, name, shop, priceMin, priceMax));
    }
}
