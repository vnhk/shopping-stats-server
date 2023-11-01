package com.shstat.shstat;

public class AttrVal<T> {
    public String attr;
    public T val;

    private AttrVal(String attr, T val) {
        this.attr = attr;
        this.val = val;
    }

    public static <T> AttrVal<T> of(String attr, T val) {
        return new AttrVal<>(attr, val);
    }
}
