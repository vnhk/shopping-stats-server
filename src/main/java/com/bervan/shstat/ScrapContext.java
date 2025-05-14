package com.bervan.shstat;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ScrapContext implements Serializable {
    private Date scrapDate;
    private String thread;
    private String contextId;
    private ConfigProduct product;
    private ConfigRoot root;

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public ConfigProduct getProduct() {
        return product;
    }

    public void setProduct(ConfigProduct product) {
        this.product = product;
    }

    public ConfigRoot getRoot() {
        return root;
    }

    public void setRoot(ConfigRoot root) {
        this.root = root;
    }

    public Date getScrapDate() {
        return scrapDate;
    }

    public void setScrapDate(Date scrapDate) {
        this.scrapDate = scrapDate;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getContextId() {
        if (Strings.isNullOrEmpty(contextId)) {
            contextId = UUID.randomUUID().toString();
        }
        return contextId;
    }
}
