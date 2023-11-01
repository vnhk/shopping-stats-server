package com.shstat.shstat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddProductApiResponse extends ApiResponse {
    protected Integer savedProducts = 0;

    public AddProductApiResponse(List<String> messages, Integer savedProducts) {
        super(messages);
        this.savedProducts = savedProducts;
    }
}
