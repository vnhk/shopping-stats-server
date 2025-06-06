package com.bervan.shstat.tokens;

import com.bervan.common.user.User;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
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

    public synchronized void createAndUpdateTokens(Product product) {
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

        if (commonUser == null) {
            commonUser = userRepository.findByUsername("COMMON_USER").get();
        }

        Set<ProductTokens> existingTokens = productTokensRepository.findByProductId(product.getId());

        Map<String, ProductTokens> existingTokenMap = existingTokens.stream()
                .collect(Collectors.toMap(
                        t -> t.getValue() + "|" + t.getFactor(),
                        t -> t
                ));

        List<ProductTokens> tokensToSave = new ArrayList<>();
        Map<String, Integer> newTokensWithFactors = new HashMap<>();
        categoryTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 3));
        nameTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 2));
        attrNameTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 1));

        for (Map.Entry<String, Integer> entry : newTokensWithFactors.entrySet()) {
            String tokenValue = entry.getKey();
            int newFactor = entry.getValue();
            String tokenKey = tokenValue + "|" + newFactor;

            ProductTokens existing = existingTokenMap.remove(tokenKey);

            if (existing == null) {
                ProductTokens token = new ProductTokens();
                token.setValue(tokenValue);
                token.setFactor(newFactor);
                token.setProductId(product.getId());
                token.addOwner(commonUser);
                tokensToSave.add(token);
            }
        }

        List<ProductTokens> tokensToDelete = new ArrayList<>(existingTokenMap.values());

        if (!tokensToDelete.isEmpty()) {
            List<Long> tokensId = tokensToDelete.stream().map(ProductTokens::getId).collect(Collectors.toList());
            productTokensRepository.deleteOwnersTokens(tokensId);
            productTokensRepository.deleteTokens(tokensId);
        }

        if (!tokensToSave.isEmpty()) {
            synchronized (this) {
                productTokensRepository.saveAll(tokensToSave);
            }
        }
    }


    public List<Long> findSimilarOffers(Long productId, int amountOfOffers) {
        Set<String> tokens = productTokensRepository.findValuesByProductId(productId);
        List<Object[]> byTokens = productTokensRepository.findByTokens(tokens, Pageable.ofSize(amountOfOffers), productId);

        return byTokens.stream().map(e -> ((Long) e[0])).collect(Collectors.toList());
    }
}
