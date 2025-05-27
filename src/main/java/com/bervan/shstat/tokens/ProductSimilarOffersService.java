package com.bervan.shstat.tokens;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductSimilarOffersService {
    private final List<? extends TokenConverter> tokenConverters;
    private final ProductTokensRepository productTokensRepository;
    private final UserRepository userRepository;
    private User commonUser;

    public ProductSimilarOffersService(List<? extends TokenConverter> tokenConverters,
                                       ProductTokensRepository productTokensRepository, UserRepository userRepository) {
        this.tokenConverters = tokenConverters;
        this.productTokensRepository = productTokensRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createAndUpdateTokens(Product product) {
        //if cache maybe here clear the cache

        //by category - 1
        Set<String> categoryTokens = product.getCategories().stream().map(String::toLowerCase).collect(Collectors.toSet());
        //by name - 0.8
        Set<String> nameTokens = new HashSet<>();
        String[] split = product.getName().toLowerCase().split(" ");
        for (TokenConverter tokenConverter : tokenConverters) {
            for (String s : split) {
                Optional<String> converted = tokenConverter.convert(s);
                if (converted.isEmpty()) {
                    nameTokens.add(s);
                } else {
                    nameTokens.add(converted.get());
                }
            }
        }

        //by attr name - 0.3
        Set<String> attrNameTokens = new HashSet<>();
        for (TokenConverter tokenConverter : tokenConverters) {
            for (ProductAttribute attribute : product.getAttributes()) {
                String name = attribute.getName();
                Optional<String> converted = tokenConverter.convert(name);
                if (converted.isEmpty()) {
                    attrNameTokens.add(name);
                } else {
                    attrNameTokens.add(converted.get());
                }
            }
        }

        productTokensRepository.deleteOwnersByProductId(product.getId());
        productTokensRepository.deleteByProductId(product.getId());

        if (commonUser == null) {
            commonUser = userRepository.findByUsername("COMMON_USER").get();
        }

        for (String categoryToken : categoryTokens) {
            ProductTokens productTokensEntityNew = new ProductTokens();
            productTokensEntityNew.setValue(categoryToken);
            productTokensEntityNew.setFactor(3);
            productTokensEntityNew.setProductId(product.getId());
            productTokensEntityNew.addOwner(commonUser);
            productTokensRepository.save(productTokensEntityNew);
        }

        for (String nameToken : nameTokens) {
            ProductTokens productTokensEntityNew = new ProductTokens();
            productTokensEntityNew.setValue(nameToken);
            productTokensEntityNew.setFactor(2);
            productTokensEntityNew.setProductId(product.getId());
            productTokensEntityNew.addOwner(commonUser);
            productTokensRepository.save(productTokensEntityNew);
        }

        for (String attrNameToken : attrNameTokens) {
            ProductTokens productTokensEntityNew = new ProductTokens();
            productTokensEntityNew.setValue(attrNameToken);
            productTokensEntityNew.setFactor(1);
            productTokensEntityNew.setProductId(product.getId());
            productTokensEntityNew.addOwner(commonUser);
            productTokensRepository.save(productTokensEntityNew);
        }
    }


    public List<Long> findSimilarOffers(Long productId, int amountOfOffers) {
        Set<String> tokens = productTokensRepository.findByProductId(productId);
        List<Object[]> byTokens = productTokensRepository.findByTokens(tokens, Pageable.ofSize(amountOfOffers));

        return byTokens.stream().map(e -> ((Long) e[0])).collect(Collectors.toList());
    }
}
