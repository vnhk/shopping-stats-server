package com.bervan.shstat;

import com.bervan.common.service.ApiKeyService;
import com.bervan.shstat.queue.AddProductsQueueParam;
import com.bervan.shstat.queue.AddProductsQueueRequest;
import com.bervan.shstat.queue.QueueMessage;
import com.bervan.shstat.queue.QueueService;
import com.bervan.shstat.response.ApiResponse;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping(path = "/products")
@PermitAll
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService searchService;
    private final QueueService queueService;
    private final ApiKeyService apiKeyService;
//    @Value("${api.keys}")
//    private List<String> API_KEYS = new ArrayList<>();

    public ProductController(ProductService productService, ProductSearchService searchService, QueueService queueService, ApiKeyService apiKeyService) {
        this.productService = productService;
        this.searchService = searchService;
        this.queueService = queueService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping(path = "/async")
    public ResponseEntity<ApiResponse> addProductsAsync(@RequestBody AddProductsQueueRequest request) {
        if (apiKeyService.getUserByAPIKey(request.getApiKey()) == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(Collections.singletonList("NO_API_KEY")));
        }
        return ResponseEntity.ok().body(queueService.sendProductMessage(new QueueMessage("AddProductsQueueParam", request.getAddProductsQueueParam(), request.getApiKey())));
    }

//    @PostMapping(path = "/refresh-materialized-views")
//    public ResponseEntity<ApiResponse> refreshMaterializedViews() {
//        return ResponseEntity.ok().body(queueService.sendProductMessage(new QueueMessage(RefreshViewQueueParam.class, null)));
//    }

    @GetMapping(path = "/categories")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Set<String>> getCategories() {
        Set<String> categories = searchService.findCategories();
        return ResponseEntity.ok(categories);
    }
}
