package com.shstat.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductDTO {
    private String name;
    private String shop;
    private String offerLink;
    private PriceDTO minPrice;
    private BigDecimal avgPrice;
    private PriceDTO maxPrice;
    private List<PriceDTO> prices;
}
