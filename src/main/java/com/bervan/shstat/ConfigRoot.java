package com.bervan.shstat;

import java.io.Serializable;
import java.util.List;


public class ConfigRoot implements Serializable {
    private String shopName;
    private String baseUrl;
    private List<ConfigProduct> products;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public List<ConfigProduct> getProducts() {
        return products;
    }

    public void setProducts(List<ConfigProduct> products) {
        this.products = products;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
