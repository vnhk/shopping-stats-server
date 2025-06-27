package com.bervan.shstat.dtomappers;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.entity.ProductListTextAttribute;
import com.bervan.shstat.repository.ActualProductsRepository;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.service.ProductStatsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.bervan.shstat.dtomappers.CommonUtils.buildPrice;

@Service
public class BaseProductAttributesMapper implements DTOMapper<Product, ProductDTO> {
    private final ActualProductsRepository actualProductsRepository;
    private Set<Long> actualProducts = new HashSet<>();

    public BaseProductAttributesMapper(ActualProductsRepository actualProductsRepository) {
        this.actualProductsRepository = actualProductsRepository;
    }

    @Scheduled(cron = "0 15 * * * *")
    public void refreshActualProducts() {
        actualProducts = new HashSet<>(actualProductsRepository.findProductIds());
    }

    @Override
    public void map(DataHolder<Product> product, DataHolder<ProductDTO> productDTO) {
        List<ProductBasedOnDateAttributes> productBasedOnDateAttributes = product.value.getProductBasedOnDateAttributes();
        List<ProductBasedOnDateAttributes> sortedPrices = productBasedOnDateAttributes.stream().sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed()).toList();

        BigDecimal avg = ProductStatsService.calculateAvgForMonthsInMemory(sortedPrices, 240);//20years...

        Optional<ProductBasedOnDateAttributes> min = sortedPrices.stream().min(Comparator.comparingInt(e -> e.getPrice().intValue()));
        Optional<ProductBasedOnDateAttributes> max = sortedPrices.stream().max(Comparator.comparingInt(e -> e.getPrice().intValue()));

        min.ifPresent(basedOnDateAttributes -> productDTO.value.setMinPrice(buildPrice(basedOnDateAttributes)));
        max.ifPresent(basedOnDateAttributes -> productDTO.value.setMaxPrice(buildPrice(basedOnDateAttributes)));
        productDTO.value.setAvgPrice(avg);

        productDTO.value.setId(product.value.getId());
        productDTO.value.setName(product.value.getName());
        productDTO.value.setShop(product.value.getShop());
        Set<ProductAttribute> attributes = product.value.getAttributes();

        String offerUrl = product.value.getOfferUrl();

        if (offerUrl == null || offerUrl.isBlank()) {
            offerUrl = ((ProductListTextAttribute) attributes.stream().
                    filter(e -> e.getName().equals("Offer Url")).findFirst().get())
                    .getValue().iterator().next();
        }

        productDTO.value.setOfferLink(getOfferUrl(product.value.getShop(), offerUrl));
        productDTO.value.setImgSrc(product.value.getImgSrc());
        productDTO.value.setCategories(product.value.getCategories());
        productDTO.value.setDiscount(getDiscountPercentage(sortedPrices, productDTO.value.getAvgPrice()));
        productDTO.value.setActual(actualProducts.contains(product.value.getId()));
    }

    private Double getDiscountPercentage(List<ProductBasedOnDateAttributes> prices, BigDecimal averagePrice) {
        if (prices == null || prices.isEmpty() || averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal currentPrice = prices.get(0).getPrice();
        BigDecimal discount = averagePrice.subtract(currentPrice);
        BigDecimal percentage = discount.divide(averagePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return percentage.doubleValue();
    }

    private String getOfferUrl(String shop, String offerUrl) {
        if (offerUrl.startsWith("http")) {
            return offerUrl;
        }

        return switch (shop) {
            case "Media Expert" -> "https://mediaexpert.pl" + offerUrl;
            case "Morele" -> "https://morele.net" + offerUrl;
            case "RTV Euro AGD" -> "https://www.euro.com.pl" + offerUrl;
            case "Centrum Rowerowe" -> "https://www.centrumrowerowe.pl" + offerUrl;
            default -> offerUrl;
        };
    }
}
