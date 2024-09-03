package com.bervan.shstat.dtomappers;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.response.PriceDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductBasedOnDateAttributePriceMapper implements DTOMapper<ProductBasedOnDateAttributes, PriceDTO> {
    @Override
    public void map(DataHolder<ProductBasedOnDateAttributes> attributes, DataHolder<PriceDTO> priceDTO) {
        priceDTO.value = CommonUtils.buildPrice(attributes.value);
    }
}
