package com.shstat.dtomappers;

import com.shstat.DataHolder;
import com.shstat.favorites.FavoriteProduct;
import com.shstat.response.PriceDTO;
import com.shstat.response.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class FavoritesBasicMapper implements DTOMapper<FavoriteProduct, ProductDTO> {
    @Override
    public void map(DataHolder<FavoriteProduct> product, DataHolder<ProductDTO> productDTO) {
        productDTO.value.setName(product.value.getProductName());
        productDTO.value.setShop(product.value.getShop());
        productDTO.value.setOfferLink(DTOMapper.getOfferUrl(product.value.getShop(), product.value.getOfferUrl()));
        productDTO.value.setImgSrc(product.value.getImgSrc());
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setDate(product.value.getScrapDate());
        priceDTO.setPrice(product.value.getPrice());
        productDTO.value.setPrices(Collections.singletonList(priceDTO));
        productDTO.value.setAvgPrice(product.value.getAvgPrice());
        productDTO.value.setDiscount(product.value.getDiscountInPercent().doubleValue());
    }
}
