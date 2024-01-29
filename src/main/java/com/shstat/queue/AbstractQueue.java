package com.shstat.queue;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public abstract class AbstractQueue<T extends Serializable> {
    public abstract void run(Serializable param);

    public boolean supports(Class<?> checkClass) {
        return (((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]).equals(checkClass);
    }
}
