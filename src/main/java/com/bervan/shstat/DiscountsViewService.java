package com.bervan.shstat;

import com.bervan.shstat.dtomappers.BaseProductAttributesMapper;
import com.bervan.shstat.dtomappers.DTOMapper;
import com.bervan.shstat.dtomappers.ProductBasedOnDateAttributePriceMapper;
import com.bervan.shstat.dtomappers.ProductPriceStatsMapper;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.repository.ProductRepository;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscountsViewService extends ViewBuilder {
    private final ProductSearchService productSearchService;
    private final Map<DiscountQueryKey, SearchApiResponse> cache =
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<DiscountQueryKey, SearchApiResponse> eldest) {
                    return this.size() > 5;
                }
            };

    @Scheduled(cron = "0 0 0 * * *")
    public void clearCacheAtMidnight() {
        cache.clear();
    }

    public DiscountsViewService(ProductSearchService productSearchService,
                                List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                                List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice) {
        super(getSet(productMappers, productBasedOnDateAttributesToPrice));
        this.productSearchService = productSearchService;
    }

    private static Set getSet(List<? extends DTOMapper<Product, ProductDTO>> productMappers,
                              List<? extends DTOMapper<ProductBasedOnDateAttributes, PriceDTO>> productBasedOnDateAttributesToPrice) {
        Set all = new HashSet<>(productMappers);
        all.addAll(productBasedOnDateAttributesToPrice);
        return all;
    }

    public SearchApiResponse findHistoricalLowPriceProducts(Pageable pageable, String category, String shop, String name) {
        Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> historicalLowProducts = productSearchService.findHistoricalLowProducts(pageable,
                category, shop, name);

        return buildResponse(pageable, historicalLowProducts);
    }

    public SearchApiResponse findXPercentLowerPriceThanHistoricalLow(Pageable pageable, Double discountMin, Double discountMax, String category, String shop,
                                                                     boolean onlyActualOffers, String name, Integer prevPriceMin, Integer prevPriceMax) {
        Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> historicalLowProducts = productSearchService.findXPercentLowerPriceThanHistoricalLow(pageable, discountMin, discountMax,
                category, shop, onlyActualOffers, name, prevPriceMin, prevPriceMax);
        return buildResponse(pageable, historicalLowProducts);
    }

    public SearchApiResponse findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable pageable, Double discountMin, Double discountMax, Integer months, String category, String shop, String name, Integer prevPriceMin, Integer prevPriceMax) {
        DiscountQueryKey key = new DiscountQueryKey(pageable.getPageNumber(), pageable.getPageSize(),
                discountMin, discountMax, months, category, shop, name, prevPriceMin, prevPriceMax);

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> historicalLowProducts =
                productSearchService.findDiscountsComparedToAVGOnPricesInLastXMonths(pageable, discountMin,
                        discountMax, months, category, shop, name, prevPriceMin, prevPriceMax);

        SearchApiResponse response = buildResponse(pageable, historicalLowProducts);
        cache.put(key, response);
        return response;
    }

    private SearchApiResponse buildResponse(Pageable pageable, Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> historicalLowProducts) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));


        Collection<Object> result = new ArrayList<>();
        Set<ProductBasedOnDateAttributes> res = map(mapper, historicalLowProducts);

        for (ProductBasedOnDateAttributes attrs : res) {
            Product product = attrs.getProduct();
            ProductDTO productDTO = new ProductDTO();
            mappersMap.get(BaseProductAttributesMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
            mappersMap.get(ProductPriceStatsMapper.class).map(DataHolder.of(product), DataHolder.of(productDTO));
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

    private Set<ProductBasedOnDateAttributes> map(ObjectMapper mapper, Page<ProductRepository.ProductBasedOnDateAttributesNativeResInterface> historicalLowProducts) {
        Set<ProductBasedOnDateAttributes> res = new HashSet<>();
        try {
            for (ProductRepository.ProductBasedOnDateAttributesNativeResInterface productBasedOnDateAttributesNativeRe : historicalLowProducts) {
                String val = mapper.writeValueAsString(productBasedOnDateAttributesNativeRe);
                ProductBasedOnDateAttributes productBasedOnDateAttributes = mapper.readValue(val, ProductBasedOnDateAttributes.class);
                Product product = productSearchService.findProductByProductBasedOnDateAttributesId(productBasedOnDateAttributes.getId());
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

    private static class DiscountQueryKey {
        private final int page;
        private final int size;
        private final Double discountMin;
        private final Double discountMax;
        private final Integer months;
        private final String category;
        private final String shop;
        private final String name;
        private final Integer prevPriceMin;
        private final Integer prevPriceMax;

        public DiscountQueryKey(int page, int size,
                                Double discountMin, Double discountMax, Integer months,
                                String category, String shop, String name,
                                Integer prevPriceMin, Integer prevPriceMax) {
            this.page = page;
            this.size = size;
            this.discountMin = discountMin;
            this.discountMax = discountMax;
            this.months = months;
            this.category = Objects.toString(category, "");
            this.shop = Objects.toString(shop, "");
            this.name = Objects.toString(name, "");
            this.prevPriceMin = prevPriceMin;
            this.prevPriceMax = prevPriceMax;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscountQueryKey)) return false;
            DiscountQueryKey that = (DiscountQueryKey) o;
            return page == that.page &&
                    size == that.size &&
                    Objects.equals(discountMin, that.discountMin) &&
                    Objects.equals(discountMax, that.discountMax) &&
                    Objects.equals(months, that.months) &&
                    category.equals(that.category) &&
                    shop.equals(that.shop) &&
                    name.equals(that.name) &&
                    Objects.equals(prevPriceMin, that.prevPriceMin) &&
                    Objects.equals(prevPriceMax, that.prevPriceMax);
        }

        @Override
        public int hashCode() {
            return Objects.hash(page, size, discountMin, discountMax, months,
                    category, shop, name, prevPriceMin, prevPriceMax);
        }
    }
}
