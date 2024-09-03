package com.bervan.shstat.queue;

import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ProductService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(BervanLogger log, ProductService productService) {
        super(log);
        this.productService = productService;
    }

    @Override
    protected void process(Serializable param) {
        log.info("Processing products started...");
        productService.addProductsByPartitions((AddProductsQueueParam) param);
        log.info("Processing products completed...");
    }
}
