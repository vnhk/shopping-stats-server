package com.shstat.shstat;

import com.shstat.shstat.entity.Product;
import com.shstat.shstat.entity.ProductAttribute;
import com.shstat.shstat.entity.ProductBasedOnDateAttributes;
import com.shstat.shstat.entity.ProductListTextAttribute;
import com.shstat.shstat.repository.ProductRepository;
import com.shstat.shstat.response.ApiResponse;
import com.shstat.shstat.response.PriceDTO;
import com.shstat.shstat.response.ProductDTO;
import com.shstat.shstat.response.SearchApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class SearchService {
    @Autowired
    private ProductRepository productRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public ApiResponse getProducts(String categories, String name, String shop, Integer priceMin, Integer priceMax) {
        if (Strings.isBlank(categories) || Strings.isBlank(name) || Strings.isBlank(shop) || priceMin == null
                || priceMax == null) {
            throw new RuntimeException("At least one search parameter is required!");
        }
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM PRODUCT p WHERE 1=1 "
//                        categories(categories) +
//                        shop(categories) +
//                        name(categories) +
//                        priceMin(categories) +
//                        priceMax(categories)
                , Product.class);

        List<Product> resultList = query.getResultList();
        return new SearchApiResponse(new ArrayList<>(), new ArrayList<>());
    }

    public ApiResponse findProductNames(String shop) {
        Set<String> productNames = productRepository.findProductNames(shop);
        return new SearchApiResponse(new ArrayList<>(), productNames);
    }

    public ApiResponse findProduct(String name, String shop) {
        List<Product> products = productRepository.findByNameContainingAndShop(name, shop);
        return new SearchApiResponse(new ArrayList<>(), map(products));
    }

    private List<ProductDTO> map(List<Product> products) {
        List<ProductDTO> res = new ArrayList<>();
        for (Product product : products) {
            res.add(map(product));
        }

        return res;
    }

    private ProductDTO map(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(product.getName());
        productDTO.setShop(product.getShop());
        ProductBasedOnDateAttributes min = productRepository.lastMinPrice(product.getName(), productDTO.getShop());
        ProductBasedOnDateAttributes max = productRepository.lastMaxPrice(product.getName(), productDTO.getShop());
        Double avg = productRepository.avgPrice(product.getName(), productDTO.getShop());
        productDTO.setMinPrice(buildPrice(min));
        productDTO.setMaxPrice(buildPrice(max));
        productDTO.setAvgPrice(BigDecimal.valueOf(avg));
        Set<ProductAttribute> attributes = product.getAttributes();
        String offerUrl = ((ProductListTextAttribute) attributes.stream().
                filter(e -> e.getName().equals("Offer Url")).findFirst().get())
                .getValue().iterator().next();
        productDTO.setOfferLink(offerUrl);
        List<PriceDTO> prices = new ArrayList<>();

        for (ProductBasedOnDateAttributes productBasedOnDateAttribute : product.getProductBasedOnDateAttributes()) {
            prices.add(buildPrice(productBasedOnDateAttribute));
        }

        productDTO.setPrices(prices);
        prices.sort(Comparator.nullsLast(
                (e1, e2) -> e2.getDate().compareTo(e1.getDate())));
        return productDTO;
    }

    private PriceDTO buildPrice(ProductBasedOnDateAttributes attr) {
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setPrice(attr.getPrice());
        priceDTO.setDate(attr.getScrapDate());
        priceDTO.setFormattedDate(attr.getFormattedScrapDate());
        return priceDTO;
    }
}
