package com.bervan.shstat.dtomappers;

import com.bervan.shstat.DataHolder;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import com.bervan.shstat.entity.ProductListTextAttribute;
import com.bervan.shstat.response.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BaseProductAttributesMapper implements DTOMapper<Product, ProductDTO> {
    @Override
    public void map(DataHolder<Product> product, DataHolder<ProductDTO> productDTO) {
        productDTO.value.setId(product.value.getId());
        productDTO.value.setName(product.value.getName());
        productDTO.value.setShop(product.value.getShop());
        Set<ProductAttribute> attributes = product.value.getAttributes();
        String offerUrl = ((ProductListTextAttribute) attributes.stream().
                filter(e -> e.getName().equals("Offer Url")).findFirst().get())
                .getValue().iterator().next();
        productDTO.value.setOfferLink(getOfferUrl(product.value.getShop(), offerUrl));
        productDTO.value.setImgSrc(product.value.getImgSrc());
        productDTO.value.setCategories(product.value.getCategories());
    }

    private static String getOfferUrl(String shop, String offerUrl) {
        if (offerUrl.startsWith("http")) {
            return offerUrl;
        }

        return switch (shop) {
            case "Media Expert" -> "https://mediaexpert.pl" + offerUrl;
            case "Morele" -> "https://morele.net" + offerUrl;
            case "RTV Euro AGD" -> "https://www.euro.com.pl" + offerUrl;
            default -> offerUrl;
        };
    }
}
