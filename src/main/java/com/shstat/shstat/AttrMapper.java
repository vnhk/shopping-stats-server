package com.shstat.shstat;

public interface AttrMapper {
    Object map(Object val);

    static Object mappingError(String fieldName) throws RuntimeException {
        throw new RuntimeException("Could not map " + fieldName + " value!");
    }

    static AttrMapper defaultMapping() {
        return (val) -> {
            return val;
        };
    }
}
