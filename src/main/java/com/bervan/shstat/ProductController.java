package com.bervan.shstat;

import com.bervan.shstat.queue.AddProductsQueueParam;
import com.bervan.shstat.queue.QueueService;
import com.bervan.shstat.response.ApiResponse;
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
    private final QueueService queueService;

    public ProductController(ProductService productService, SearchService searchService, QueueService queueService) {
        this.productService = productService;
        this.searchService = searchService;
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addProducts(@RequestBody List<Map<String, Object>> products) {
        return ResponseEntity.ok(productService.addProducts(products));
    }

    @PostMapping(path = "/async")
    public ResponseEntity<ApiResponse> addProductsAsync(@RequestBody AddProductsQueueParam products) {
        return ResponseEntity.ok().body(queueService.addProductsAsync(products));
    }

    public ApiResponse refreshMaterializedViews() {
        return queueService.refreshMaterializedViews();
    }

    @GetMapping(path = "/categories")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Set<String>> getCategories() {
        Set<String> categories = searchService.findCategories();
        return ResponseEntity.ok(categories);
    }
}
