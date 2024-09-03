package com.bervan.shstat.queue;


import com.bervan.core.model.BervanLogger;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Date;

public abstract class AbstractQueue<T extends Serializable> {
    protected final BervanLogger log;

    protected AbstractQueue(BervanLogger log) {
        this.log = log;
    }

    public final void run(Serializable param) {
        Date startDate = new Date();
        process(param);
        Date endDate = new Date();
        long diffTimeInSeconds = diffTimeInSeconds(startDate, endDate);
        log.info("Task duration: " + diffTimeInSeconds + " (s)");
    }

    protected abstract void process(Serializable param);

    protected long diffTimeInSeconds(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / 1000;
    }

    public boolean supports(Class<?> checkClass) {
        return (((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]).equals(checkClass);
    }
}
