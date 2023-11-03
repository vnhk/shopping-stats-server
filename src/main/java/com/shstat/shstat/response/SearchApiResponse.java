package com.shstat.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SearchApiResponse extends ApiResponse {
    private Integer allFound;
    private Integer page;
    private Integer pageSize;
    private Integer currentFound;
    private List<Map<String, Object>> products;

    public SearchApiResponse(List<String> messages, List<Map<String, Object>> products) {
        super(messages);
        this.setProducts(products);
    }

    public void setProducts(List<Map<String, Object>> products) {
        this.products = products;
        this.currentFound = products.size();
    }
}
