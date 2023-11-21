package com.shstat.dtomappers;

import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.repository.ProductRepository;
import com.shstat.response.ProductDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.shstat.dtomappers.CommonUtils.buildPrice;

@Service
public class ProductPriceStatsMapper implements DTOMapper<Product, ProductDTO> {
    private final ProductRepository productRepository;

    public ProductPriceStatsMapper(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void map(Product product, ProductDTO productDTO) {
        ProductBasedOnDateAttributes min = productRepository.lastMinPrice(product.getName(), product.getShop());
        ProductBasedOnDateAttributes max = productRepository.lastMaxPrice(product.getName(), product.getShop());
        Double avg = productRepository.avgPrice(product.getName(), product.getShop());
        productDTO.setMinPrice(buildPrice(min));
        productDTO.setMaxPrice(buildPrice(max));
        if (avg != null) {
            productDTO.setAvgPrice(BigDecimal.valueOf(avg));
        } else {
            productDTO.setAvgPrice(BigDecimal.valueOf(-1));
        }
    }
}
