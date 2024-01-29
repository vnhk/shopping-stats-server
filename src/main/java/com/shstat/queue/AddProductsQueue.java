package com.shstat.queue;

import com.shstat.ProductService;
import org.springframework.stereotype.Service;

@Service
public class AddProductsQueue extends AbstractQueue<AddProductsQueueParam> {
    private final ProductService productService;

    public AddProductsQueue(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(Object param) {
        productService.addProductsByPartitions((AddProductsQueueParam) param);
    }
}
