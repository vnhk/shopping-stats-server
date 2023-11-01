package com.shstat.shstat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {
    protected List<String> messages;

    public ApiResponse(List<String> messages) {
        this.messages = messages;
    }
}
