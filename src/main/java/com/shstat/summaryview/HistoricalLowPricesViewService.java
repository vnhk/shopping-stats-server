package com.shstat.summaryview;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shstat.DataHolder;
import com.shstat.SearchService;
import com.shstat.ViewBuilder;
import com.shstat.dtomappers.BaseProductAttributesMapper;
import com.shstat.dtomappers.DTOMapper;
import com.shstat.dtomappers.ProductBasedOnDateAttributePriceMapper;
import com.shstat.entity.Product;
import com.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.repository.ProductRepository;
import com.shstat.response.PriceDTO;
import com.shstat.response.ProductDTO;
import com.shstat.response.SearchApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public SearchApiResponse findHistoricalLowPriceProducts(Pageable pageable) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));


        Collection<Object> result = new ArrayList<>();
        Page<ProductRepository.ProductBasedOnDateAttributesNativeRes> historicalLowProducts = searchService.findHistoricalLowProducts(pageable);
        Set<ProductBasedOnDateAttributes> res = map(mapper, historicalLowProducts);

        for (ProductBasedOnDateAttributes attrs : res) {
            Product product = attrs.getProduct();
            ProductDTO productDTO = new ProductDTO();
            mappersMap.get(BaseProductAttributesMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
            DataHolder<PriceDTO> priceHolder = DataHolder.of(new PriceDTO());
            mappersMap.get(ProductBasedOnDateAttributePriceMapper.class).map(DataHolder.of(attrs), priceHolder);
            productDTO.setPrices(Collections.singletonList(priceHolder.value));
            result.add(productDTO);
        }

        return SearchApiResponse.builder()
                .items(result)
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .allFound(historicalLowProducts.getTotalElements())
                .allPages(historicalLowProducts.getTotalPages())
                .build();
    }

    private Set<ProductBasedOnDateAttributes> map(ObjectMapper mapper, Page<ProductRepository.ProductBasedOnDateAttributesNativeRes> historicalLowProducts) {
        Set<ProductBasedOnDateAttributes> res = new HashSet<>();
        try {
            for (ProductRepository.ProductBasedOnDateAttributesNativeRes productBasedOnDateAttributesNativeRe : historicalLowProducts) {
                String val = mapper.writeValueAsString(productBasedOnDateAttributesNativeRe);
                ProductBasedOnDateAttributes productBasedOnDateAttributes = mapper.readValue(val, ProductBasedOnDateAttributes.class);
                Product product = searchService.findProductByProductBasedOnDateAttributesId(productBasedOnDateAttributes.getId());
                if (product == null) {
                    throw new RuntimeException("Could not find product based on product attributes id!");
                }
                productBasedOnDateAttributes.setProduct(product);
                res.add(productBasedOnDateAttributes);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return res;
    }
}
