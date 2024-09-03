package com.bervan.shstat.dtomappers;

import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.response.PriceDTO;

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
