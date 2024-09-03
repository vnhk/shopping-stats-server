package com.bervan.shstat.response;


import java.util.List;

public class AddProductApiResponse extends ApiResponse {
    protected Integer savedProducts = 0;
    protected Integer requestedToSave = 0;

    public AddProductApiResponse(List<String> messages, Integer savedProducts, Integer requestedToSave) {
        super(messages);
        this.savedProducts = savedProducts;
        this.requestedToSave = requestedToSave;
    }

    public Integer getSavedProducts() {
        return savedProducts;
    }

    public void setSavedProducts(Integer savedProducts) {
        this.savedProducts = savedProducts;
    }

    public Integer getRequestedToSave() {
        return requestedToSave;
    }

    public void setRequestedToSave(Integer requestedToSave) {
        this.requestedToSave = requestedToSave;
    }
}
