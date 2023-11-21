package com.shstat;

import com.shstat.dtomappers.DTOMapper;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ViewBuilder {
    protected final Set<? extends DTOMapper> mappers;

    protected ViewBuilder(Collection<? extends DTOMapper> mappers, Set<Class<? extends DTOMapper>> usedMappers) {
        this.mappers = mappers.stream().filter(e -> usedMappers.contains(e.getClass()))
                .collect(Collectors.toSet());
    }
}
