package com.shstat.dtomappers;

import com.shstat.DataHolder;
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
    public void map(DataHolder<Product> product, DataHolder<ProductDTO> productDTO) {
        ProductBasedOnDateAttributes min = productRepository.lastMinPrice(product.value.getName(), product.value.getShop());
        ProductBasedOnDateAttributes max = productRepository.lastMaxPrice(product.value.getName(), product.value.getShop());
        Double avg = productRepository.avgPrice(product.value.getName(), product.value.getShop());
        productDTO.value.setMinPrice(buildPrice(min));
        productDTO.value.setMaxPrice(buildPrice(max));
        if (avg != null) {
            productDTO.value.setAvgPrice(BigDecimal.valueOf(avg));
        } else {
            productDTO.value.setAvgPrice(BigDecimal.valueOf(-1));
        }
    }
}
