package com.bervan.shstat.queue;

import java.io.Serializable;

public class RefreshViewQueueParam implements Serializable {
    private String viewName;

    public RefreshViewQueueParam(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }
}
