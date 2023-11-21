package com.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class PriceDTO {
    private Date date;
    private String formattedDate;
    private BigDecimal price;
}
