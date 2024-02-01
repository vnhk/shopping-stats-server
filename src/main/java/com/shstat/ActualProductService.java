package com.shstat;

import com.shstat.entity.ActualProduct;
import com.shstat.entity.Product;
import com.shstat.repository.ActualProductsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.shstat.ProductService.productPerDateAttributeProperties;

@Service
public class ActualProductService {
    private static final Integer currentDateOffsetInDays = 2;
    private final ActualProductsRepository actualProductsRepository;

    public ActualProductService(ActualProductsRepository actualProductsRepository) {
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateActualProducts(Map<String, Object> product, Product mappedProduct) {
        Date date = (Date) productPerDateAttributeProperties.stream().filter(e -> e.attr.equals("Date")).findFirst()
                .get().mapper.map(product);
        Optional<ActualProduct> actualProduct = actualProductsRepository.findByProductId(mappedProduct.getId());
        if (actualProduct.isPresent()) {
            if (actualProduct.get().getScrapDate().before(date)) {
                actualProduct.get().setScrapDate(date);
                actualProductsRepository.save(actualProduct.get());
            }
        } else {
            ActualProduct newAP = new ActualProduct();
            newAP.setProductId(mappedProduct.getId());
            newAP.setScrapDate(date);
            actualProductsRepository.save(newAP);
        }

        updateActualProducts();
    }

    private void updateActualProducts() {
        actualProductsRepository.deleteNotActualProducts(currentDateOffsetInDays);
    }
}
