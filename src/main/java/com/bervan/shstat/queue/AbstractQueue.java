package com.bervan.shstat.queue;


import com.bervan.common.service.ApiKeyService;
import com.bervan.logging.JsonLogger;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractQueue<T extends Serializable> {
    protected final ApiKeyService apiKeyService;
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");
    protected String supports;

    protected AbstractQueue(ApiKeyService apiKeyService, String supports) {
        this.apiKeyService = apiKeyService;
        this.supports = supports;
    }

    public final void run(Serializable param) {
        Date startDate = new Date();
        process(param);
        Date endDate = new Date();
        long diffTimeInSeconds = diffTimeInSeconds(startDate, endDate);
        log.info("[" + supports + "] Task duration: " + diffTimeInSeconds + " (s)");
    }

    protected abstract void process(Serializable param);

    protected long diffTimeInSeconds(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / 1000;
    }

    public boolean supports(String paramSupports) {
        return supports.equals(paramSupports);
    }
}
