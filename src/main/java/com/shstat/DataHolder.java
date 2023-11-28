package com.shstat;

public class DataHolder<T> {
    public T value;

    public DataHolder(T value) {
        this.value = value;
    }

    public static <T> DataHolder<T> of(T value) {
        return new DataHolder<>(value);
    }
}
