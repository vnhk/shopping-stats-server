package com.bervan.shstat.response;


import java.util.List;

public class ApiResponse {
    protected List<String> messages;

    public ApiResponse(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
