package com.bervan.shstat;

public class AttrFieldMappingVal<T> {
    public String attr;
    public T val;
    public AttrMapper mapper = AttrMapper.defaultMapping();

    private AttrFieldMappingVal(String attr, T val) {
        this.attr = attr;
        this.val = val;
    }

    private AttrFieldMappingVal(String attr, T val, AttrMapper mapper) {
        this.attr = attr;
        this.val = val;
        this.mapper = mapper;
    }

    public static <T> AttrFieldMappingVal<T> of(String attr, T val) {
        return new AttrFieldMappingVal<>(attr, val);
    }
    public static <T> AttrFieldMappingVal<T> of(String attr, T val, AttrMapper mapper) {
        return new AttrFieldMappingVal<>(attr, val, mapper);
    }
}
