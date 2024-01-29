package com.shstat.queue;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Date;

@Slf4j
public abstract class AbstractQueue<T extends Serializable> {
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
