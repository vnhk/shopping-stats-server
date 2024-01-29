package com.shstat.queue;

import com.shstat.ProductService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(Serializable param) {
        productService.addProductsByPartitions((AddProductsQueueParam) param);
    }
}
