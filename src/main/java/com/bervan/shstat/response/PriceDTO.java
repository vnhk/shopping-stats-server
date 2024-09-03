package com.bervan.shstat.response;

import java.math.BigDecimal;
import java.util.Date;

public class PriceDTO {
    private Date date;
    private String formattedDate;
    private BigDecimal price;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
