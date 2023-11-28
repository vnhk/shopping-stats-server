package com.shstat.summaryview;

import com.shstat.DataHolder;
import com.shstat.SearchService;
import com.shstat.ViewBuilder;
import com.shstat.dtomappers.BaseProductAttributesMapper;
import com.shstat.dtomappers.DTOMapper;
import com.shstat.dtomappers.ProductBasedOnDateAttributePriceMapper;
import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.response.PriceDTO;
import com.shstat.response.ProductDTO;
import com.shstat.response.SearchApiResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HistoricalLowPricesViewService extends ViewBuilder {
    private final SearchService searchService;

    public HistoricalLowPricesViewService(SearchService searchService,
                                          List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                                          List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice) {
        super(getSet(productMappers, productBasedOnDateAttributesToPrice), Set.of(BaseProductAttributesMapper.class, ProductBasedOnDateAttributePriceMapper.class));
        this.searchService = searchService;
    }

    private static Set getSet(List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                              List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice) {
        Set all = new HashSet<>(productMappers);
        all.addAll(productBasedOnDateAttributesToPrice);
        return all;
    }

    public SearchApiResponse findHistoricalLowPriceProducts() {
        List<ProductDTO> result = new ArrayList<>();
        Set<ProductBasedOnDateAttributes> productsAttrPerDate = searchService.findHistoricalLowProducts();
        for (ProductBasedOnDateAttributes attrs : productsAttrPerDate) {
            Product product = attrs.getProduct();
            ProductDTO productDTO = new ProductDTO();
            mappersMap.get(BaseProductAttributesMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
            DataHolder<PriceDTO> priceHolder = DataHolder.of(new PriceDTO());
            mappersMap.get(ProductBasedOnDateAttributePriceMapper.class).map(DataHolder.of(attrs), priceHolder);
            productDTO.setPrices(Collections.singletonList(priceHolder.value));
            result.add(productDTO);
        }
        return new SearchApiResponse(result);
    }
}
