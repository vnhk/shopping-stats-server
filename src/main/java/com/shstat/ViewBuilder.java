package com.shstat;

import com.shstat.dtomappers.DTOMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ViewBuilder {
    protected final Set<? extends DTOMapper> mappers;
    protected final Map<Class<? extends DTOMapper>, DTOMapper> mappersMap = new HashMap<>();

    protected ViewBuilder(Collection<? extends DTOMapper<?, ?>> mappers, Set<Class<? extends DTOMapper<?, ?>>> usedMappers) {
        this.mappers = mappers.stream().filter(e -> usedMappers.contains(e.getClass()))
                .collect(Collectors.toSet());
        for (DTOMapper mapper : this.mappers) {
            this.mappersMap.put(mapper.getClass(), mapper);
        }
    }
}
