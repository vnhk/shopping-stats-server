package com.bervan.shstat.queue;

import java.io.Serializable;

public class QueueMessage implements Serializable {
    private Class<?> aClass;
    private Serializable body;

    public QueueMessage(Class<?> aClass, Serializable body) {
        this.aClass = aClass;
        this.body = body;
    }

    public void setaClass(Class<?> aClass) {
        this.aClass = aClass;
    }

    public void setBody(Serializable body) {
        this.body = body;
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public Serializable getBody() {
        return body;
    }
}

