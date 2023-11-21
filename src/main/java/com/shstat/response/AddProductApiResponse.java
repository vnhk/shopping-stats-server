package com.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddProductApiResponse extends ApiResponse {
    protected Integer savedProducts = 0;
    protected Integer requestedToSave = 0;

    public AddProductApiResponse(List<String> messages, Integer savedProducts, Integer requestedToSave) {
        super(messages);
        this.savedProducts = savedProducts;
        this.requestedToSave = requestedToSave;
    }
}
