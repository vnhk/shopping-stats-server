package com.shstat.dtomappers;

import com.shstat.DataHolder;

public interface DTOMapper<T, DTO> {
    void map(DataHolder<T> t, DataHolder<DTO> dto);
}
