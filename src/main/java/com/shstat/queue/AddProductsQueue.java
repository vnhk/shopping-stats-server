package com.shstat.queue;

import com.shstat.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Slf4j
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void process(Serializable param) {
        log.info("Processing products started...");
        productService.addProductsByPartitions((AddProductsQueueParam) param);
        log.info("Processing products completed...");
    }
}
