package com.shstat.queue;

import com.shstat.ProductService;
import com.shstat.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class RefreshViewQueue extends AbstractQueue<RefreshViewQueueParam> {
    private final ProductService productService;
    private final ProductRepository productRepository;

    public RefreshViewQueue(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Override
    public void run(Object object) {
        productRepository.refreshHistoricalLowPricesTable();
        productRepository.refreshLowerPricesThanHistoricalLowTable();
        productRepository.refreshLowerThanAVGForLastMonth();

        productService.lowerThanAVGForLastMonth();
    }
}
