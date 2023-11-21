package com.shstat.dtomappers;

import com.shstat.entity.Product;
import com.shstat.entity.ProductAttribute;
import com.shstat.entity.ProductListTextAttribute;
import com.shstat.response.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BaseProductAttributesMapper implements DTOMapper<Product, ProductDTO> {
    @Override
    public void map(Product product, ProductDTO productDTO) {
        productDTO.setName(product.getName());
        productDTO.setShop(product.getShop());
        Set<ProductAttribute> attributes = product.getAttributes();
        String offerUrl = ((ProductListTextAttribute) attributes.stream().
                filter(e -> e.getName().equals("Offer Url")).findFirst().get())
                .getValue().iterator().next();
        productDTO.setOfferLink(offerUrl);
    }
}
