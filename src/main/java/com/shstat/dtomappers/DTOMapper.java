package com.shstat.dtomappers;

public interface DTOMapper<T, DTO> {
    void map(T t, DTO dto);
}
