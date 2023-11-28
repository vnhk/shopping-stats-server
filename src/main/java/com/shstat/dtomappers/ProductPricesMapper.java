package com.shstat.dtomappers;

import com.shstat.DataHolder;
import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.response.PriceDTO;
import com.shstat.response.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.shstat.dtomappers.CommonUtils.buildPrice;

@Service
public class ProductPricesMapper implements DTOMapper<Product, ProductDTO> {
    @Override
    public void map(DataHolder<Product> product, DataHolder<ProductDTO> productDTO) {
        List<PriceDTO> prices = new ArrayList<>();
        for (ProductBasedOnDateAttributes productBasedOnDateAttribute : product.value.getProductBasedOnDateAttributes()) {
            prices.add(buildPrice(productBasedOnDateAttribute));
        }
        productDTO.value.setPrices(prices);
        prices.sort(Comparator.nullsLast(
                (e1, e2) -> e2.getDate().compareTo(e1.getDate())));
    }
}
