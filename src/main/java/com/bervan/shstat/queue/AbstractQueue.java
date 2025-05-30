package com.bervan.shstat.queue;


import com.bervan.common.service.ApiKeyService;
import com.bervan.core.model.BervanLogger;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractQueue<T extends Serializable> {
    protected final BervanLogger log;
    protected String supports;
    protected final ApiKeyService apiKeyService;

    protected AbstractQueue(BervanLogger log, ApiKeyService apiKeyService, String supports) {
        this.log = log;
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
