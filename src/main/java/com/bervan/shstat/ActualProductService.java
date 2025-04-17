package com.bervan.shstat;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.ActualProduct;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ActualProductsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static com.bervan.shstat.ProductService.productPerDateAttributeProperties;

@Service
public class ActualProductService {
    private static final Integer currentDateOffsetInDays = 2;
    private final ActualProductsRepository actualProductsRepository;

    public ActualProductService(ActualProductsRepository actualProductsRepository) {
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateActualProducts(Object date, Product mappedProduct, User commonUser) {
        Date scrapDate = (Date) productPerDateAttributeProperties.stream().filter(e -> e.attr.equals("Date")).findFirst()
                .get().mapper.map(date);
        Optional<ActualProduct> actualProduct = actualProductsRepository.findByProductId(mappedProduct.getId());
        if (actualProduct.isPresent()) {
            if (actualProduct.get().getScrapDate().before(scrapDate)) {
                actualProduct.get().setScrapDate(scrapDate);
                if (!actualProduct.get().getOwners().contains(commonUser)) {
                    actualProduct.get().addOwner(commonUser);
                }
                actualProductsRepository.save(actualProduct.get());
            }
        } else {
            ActualProduct newAP = new ActualProduct();
            newAP.addOwner(commonUser);
            newAP.setProductId(mappedProduct.getId());
            newAP.setScrapDate(scrapDate);
            actualProductsRepository.save(newAP);
        }

        updateActualProducts();
    }

    @Transactional
    public void updateActualProducts() {
        actualProductsRepository.deleteRelatedProductOwners(currentDateOffsetInDays);
        actualProductsRepository.deleteNotActualProducts(currentDateOffsetInDays);
    }
}
