package com.bervan.shstat;

import java.io.Serializable;


public class ScrapTime implements Serializable {
    private Integer hours;

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Integer getHours() {
        return hours;
    }
}
