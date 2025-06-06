package com.bervan.shstat.dtomappers;

import com.bervan.shstat.DataHolder;

public interface DTOMapper<T, DTO> {
    void map(DataHolder<T> t, DataHolder<DTO> dto);
}
