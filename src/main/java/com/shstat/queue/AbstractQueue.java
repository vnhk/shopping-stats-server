package com.shstat.queue;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractQueue<T> {
    public abstract void run(Object param);

    public boolean supports(Class<?> checkClass) {
        return (((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]).equals(checkClass);
    }
}
