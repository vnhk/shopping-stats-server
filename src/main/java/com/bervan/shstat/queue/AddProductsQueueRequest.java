package com.bervan.shstat.queue;


import java.io.Serializable;

public class AddProductsQueueRequest implements Serializable {
    AddProductsQueueParam addProductsQueueParam;
    String apiKey;

    public AddProductsQueueParam getAddProductsQueueParam() {
        return addProductsQueueParam;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setAddProductsQueueParam(AddProductsQueueParam addProductsQueueParam) {
        this.addProductsQueueParam = addProductsQueueParam;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
