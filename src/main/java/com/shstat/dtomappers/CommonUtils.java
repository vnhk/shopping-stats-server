package com.shstat.dtomappers;

import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.response.PriceDTO;

class CommonUtils {
    static PriceDTO buildPrice(ProductBasedOnDateAttributes attr) {
        if (attr == null) {
            return null;
        }
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setPrice(attr.getPrice());
        priceDTO.setDate(attr.getScrapDate());
        priceDTO.setFormattedDate(attr.getFormattedScrapDate());
        return priceDTO;
    }
}
