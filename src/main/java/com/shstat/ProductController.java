package com.shstat;

import com.shstat.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @PostMapping(path = "/refresh-materialized-views")
    public ResponseEntity<ApiResponse> refreshMaterializedViews() {
        return ResponseEntity.ok(productService.refreshMaterializedViews());
    }

    @GetMapping(path = "/categories")
    public ResponseEntity<Set<String>> getCategories() {
        Set<String> categories = searchService.findCategories();
        return ResponseEntity.ok(categories);
    }
}
