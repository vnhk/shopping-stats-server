package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.logging.BaseProcessContext;
import com.bervan.logging.JsonLogger;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.service.ProductService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final JsonLogger log = JsonLogger.getLogger(getClass());
    private final ProductService productService;

    public AddProductsQueue(ProductService productService, ApiKeyService apiKeyService) {
        super(apiKeyService, "AddProductsQueueParam");
        this.productService = productService;
    }

    @Override
    protected void process(Serializable param) {
        BaseProcessContext addProducts = BaseProcessContext.builder().processName("addProducts").build();
        if (param instanceof List<?> list) {
            addProductsByPartitions((List<Map<String, Object>>) param, addProducts);
        } else {
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) param;
            addProductsByPartitions((List<Map<String, Object>>) data.get("addProductsQueueParam"), addProducts);
        }
    }

    public void addProductsByPartitions(List<Map<String, Object>> products, BaseProcessContext addProductsContext) {
        log.info(addProductsContext.map(), "Processing started for: {} products", products.size());
        List<List<Map<String, Object>>> partition = Lists.partition(products, 3);
        List<CompletableFuture<List<Product>>> futures = new ArrayList<>();
        List<Product> allMapped = new ArrayList<>();

        for (List<Map<String, Object>> p : partition) {
            futures.add(productService.addProductsAsync(p, addProductsContext));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.MINUTES);

            for (CompletableFuture<List<Product>> future : futures) {
                allMapped.addAll(future.get());
            }
        } catch (TimeoutException e) {
            log.error(addProductsContext.map(), "Timeout while waiting for async tasks");
        } catch (Exception e) {
            log.error(addProductsContext.map(), "Error while processing async tasks", e);
        }

        productService.updateScrapAudit(allMapped, addProductsContext);
        log.info(addProductsContext.map(), "Processing ended for: {} products", allMapped.size());
    }
}
