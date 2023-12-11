package com.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProductDTO {
    private String name;
    private String shop;
    private String offerLink;
    private String imgSrc;
    private Set<String> categories;
    private PriceDTO minPrice;
    private BigDecimal avgPrice;
    private PriceDTO maxPrice;
    private List<PriceDTO> prices;
}
