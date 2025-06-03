package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductService;
import com.bervan.shstat.entity.Product;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(BervanLogger log, ProductService productService, ApiKeyService apiKeyService) {
        super(log, apiKeyService, "AddProductsQueueParam");
        this.productService = productService;
    }

    @Override
    protected void process(Serializable param) {
        if (param instanceof List<?> list) {
            addProductsByPartitions((List<Map<String, Object>>) param);
        } else {
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) param;
            addProductsByPartitions((List<Map<String, Object>>) data.get("addProductsQueueParam"));
        }
    }

    private void addProductsByPartitions(List<Map<String, Object>> products) {
        log.info("Processing started for: {} products", products.size());
        List<List<Map<String, Object>>> partition = Lists.partition(products, 3);
        List<CompletableFuture<List<Product>>> futures = new ArrayList<>();
        List<Product> allMapped = new ArrayList<>();

        for (List<Map<String, Object>> p : partition) {
            futures.add(productService.addProductsAsync(p));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1, TimeUnit.MINUTES);

            for (CompletableFuture<List<Product>> future : futures) {
                allMapped.addAll(future.get());
            }
        } catch (TimeoutException e) {
            log.error("Timeout while waiting for async tasks");
        } catch (Exception e) {
            log.error("Error while processing async tasks", e);
        }

        productService.updateScrapAudit(allMapped);
        log.info("Processing ended for: {} products", allMapped.size());
    }
}
