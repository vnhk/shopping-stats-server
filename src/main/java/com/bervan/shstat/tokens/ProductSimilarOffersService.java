package com.bervan.shstat.tokens;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductAttribute;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSimilarOffersService {
    private final List<? extends TokenConverter> tokenConverters;
    private final ProductTokensRepository productTokensRepository;
    private final List<ProductTokens> tokensToSave = new ArrayList<>();
    private final List<ProductTokens> tokensToDelete = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ProductSimilarOffersService(List<? extends TokenConverter> tokenConverters,
                                       ProductTokensRepository productTokensRepository) {
        this.tokenConverters = tokenConverters;
        this.productTokensRepository = productTokensRepository;
    }

    @Transactional
    public void createAndUpdateTokens(Product product, User commonUser) {
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

        Set<ProductTokens> existingTokens = productTokensRepository.findByProductId(product.getId());

        Map<String, ProductTokens> existingTokenMap = existingTokens.stream()
                .collect(Collectors.toMap(
                        t -> t.getValue() + "|" + t.getFactor(),
                        t -> t
                ));

        Map<String, Integer> newTokensWithFactors = new HashMap<>();
        categoryTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 3));
        nameTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 2));
        attrNameTokens.forEach(token -> newTokensWithFactors.put(token.toLowerCase(), 1));

        lock.lock();
        try {
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

            tokensToDelete.addAll(existingTokenMap.values());
        } finally {
            lock.unlock();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void processTokensInDb() {
        try {
            if (lock.tryLock(5, TimeUnit.MINUTES)) {
                try {
                    if (!tokensToDelete.isEmpty()) {
                        List<Long> tokensId = tokensToDelete.stream().map(ProductTokens::getId).collect(Collectors.toList());
                        productTokensRepository.deleteOwnersTokens(tokensId);
                        productTokensRepository.deleteTokens(tokensId);
                        tokensToDelete.clear();
                    }

                    if (!tokensToSave.isEmpty()) {
                        productTokensRepository.saveAll(tokensToSave);
                        tokensToSave.clear();
                    }
                } catch (Exception e) {
                    log.error("Failed to flush product tokens", e);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Flush skipped due to active write lock");
            }
        } catch (InterruptedException e) {
            log.warn("InterruptedException for write lock");
        }
    }


    public List<Long> findSimilarOffers(Long productId, int amountOfOffers) {
        Set<String> tokens = productTokensRepository.findValuesByProductId(productId);
        List<Object[]> byTokens = productTokensRepository.findByTokens(tokens, Pageable.ofSize(amountOfOffers), productId);

        return byTokens.stream().map(e -> ((Long) e[0])).collect(Collectors.toList());
    }
}
