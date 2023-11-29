package com.shstat.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class SearchApiResponse<T> extends ApiResponse {
    private Long allFound;
    private Integer page;
    private Integer pageSize;
    private Integer allPages = 0;
    private Integer currentFound;
    private Collection<T> items;

    public static <T> SearchApiResponseBuilder<T> builder() {
        return new SearchApiResponseBuilder<>();
    }

    public static class SearchApiResponseBuilder<T> {
        private Long allFound = 0L;
        private Integer page = 0;
        private Integer allPages = 0;
        private Integer pageSize = 0;
        private Integer currentFound = 0;
        private Collection<T> items = new ArrayList<>();
        private List<String> messages = new ArrayList<>();

        private SearchApiResponseBuilder() {

        }

        public SearchApiResponseBuilder messages(List<String> messages) {
            this.messages = messages;
            return this;
        }

        public SearchApiResponseBuilder allFound(Long allFound) {
            this.allFound = allFound;
            return this;
        }

        public SearchApiResponseBuilder allPages(Integer allPages) {
            this.allPages = allPages;
            return this;
        }

        public SearchApiResponseBuilder page(Integer page) {
            this.page = page;
            return this;
        }

        public SearchApiResponseBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public SearchApiResponseBuilder items(Collection<T> items) {
            this.items = items;
            this.currentFound = items.size();
            return this;
        }

        public SearchApiResponse<T> build() {
            return new SearchApiResponse<>(messages, allFound, allPages, page, pageSize, currentFound, items);
        }
    }

    private SearchApiResponse(List<String> messages, Long allFound, Integer allPages, Integer page, Integer pageSize, Integer currentFound, Collection<T> items) {
        super(messages);
        this.allFound = allFound;
        this.allPages = allPages;
        this.page = page;
        this.pageSize = pageSize;
        this.currentFound = currentFound;
        this.items = items;
    }
}
