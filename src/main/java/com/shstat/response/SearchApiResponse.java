package com.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class SearchApiResponse<T> extends ApiResponse {
    private Integer allFound;
    private Integer page;
    private Integer pageSize;
    private Integer currentFound;
    private Collection<T> items;

    public SearchApiResponse(List<String> messages, Collection<T> items) {
        super(messages);
        this.setItems(items);
        this.page = 1;
        this.pageSize = items.size();
        this.allFound = items.size();
    }

    public SearchApiResponse(Collection<T> items) {
        super(new ArrayList<>());
        this.setItems(items);
        this.page = 1;
        this.pageSize = items.size();
        this.allFound = items.size();
    }

    public SearchApiResponse(List<String> messages, Collection<T> items, Integer page,
                             Integer pageSize, Integer allFound) {
        super(messages);
        this.setItems(items);
        this.page = page;
        this.pageSize = pageSize;
        this.allFound = allFound;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
        this.currentFound = items.size();
    }
}
