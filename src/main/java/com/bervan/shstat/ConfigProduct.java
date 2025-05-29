package com.bervan.shstat;

import java.io.Serializable;
import java.util.Set;

public class ConfigProduct implements Serializable {
    private String name;
    private Set<String> categories;
    private String url;
    private Integer minPrice;
    private Integer maxPrice;
    private ScrapTime scrapTime;

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScrapTime getScrapTime() {
        return scrapTime;
    }

    public void setScrapTime(ScrapTime scrapTime) {
        this.scrapTime = scrapTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
}
