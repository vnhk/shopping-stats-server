package com.bervan.shstat;

import com.bervan.shstat.dtomappers.BaseProductAttributesMapper;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.dtomappers.ProductPricesMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ViewBuilder {
    protected final Set<? extends DTOMapper> mappers;
    protected final Map<Class<? extends DTOMapper>, DTOMapper> mappersMap = new HashMap<>();
    public static final Set<Class<? extends DTOMapper<?, ?>>> PRODUCT_WITH_DETAILS_AND_PRICE_HISTORY_MAPPERS
            = Set.of(BaseProductAttributesMapper.class, ProductPricesMapper.class);

    protected ViewBuilder(Collection<? extends DTOMapper<?, ?>> mappers) {
        this.mappers = mappers.stream().collect(Collectors.toSet());
        for (DTOMapper mapper : this.mappers) {
            this.mappersMap.put(mapper.getClass(), mapper);
        }
    }

    protected Set<? extends DTOMapper> mappersSubSet(Set<Class<? extends DTOMapper<?, ?>>> usedMappers) {
        return mappers.stream().filter(e -> usedMappers.contains(e.getClass())).collect(Collectors.toSet());
    }
}
