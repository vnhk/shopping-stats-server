package com.bervan.shstat;

import com.bervan.shstat.queue.AddProductsQueueParam;
import com.bervan.shstat.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

//@RestController
//@RequestMapping(path = "/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addProducts(@RequestBody List<Map<String, Object>> products) {
        return ResponseEntity.ok(productService.addProducts(products));
    }

    @GetMapping(path = "/categories")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Set<String>> getCategories() {
        Set<String> categories = productSearchService.findCategories();
        return ResponseEntity.ok(categories);
    }
}
