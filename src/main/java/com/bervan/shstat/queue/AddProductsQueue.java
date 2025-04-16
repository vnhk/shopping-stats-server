package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(BervanLogger log, ProductService productService, ApiKeyService apiKeyService) {
        super(log, apiKeyService);
        this.productService = productService;
    }

    @Override
    protected void process(Serializable param) {
        log.info("Processing products started...");
        AddProductsQueueRequest data = (AddProductsQueueRequest) param;
        productService.addProductsByPartitions((List<Map<String, Object>>) data.getAddProductsQueueParam(), apiKeyService.getUserByAPIKey(data.getApiKey()));
        log.info("Processing products completed...");
    }
}
