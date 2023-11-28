package com.shstat.dtomappers;

import com.shstat.DataHolder;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.response.PriceDTO;
import org.springframework.stereotype.Service;

import static com.shstat.dtomappers.CommonUtils.buildPrice;

@Service
public class ProductBasedOnDateAttributePriceMapper implements DTOMapper<ProductBasedOnDateAttributes, PriceDTO> {
    @Override
    public void map(DataHolder<ProductBasedOnDateAttributes> attributes, DataHolder<PriceDTO> priceDTO) {
        priceDTO.value = buildPrice(attributes.value);
    }
}
