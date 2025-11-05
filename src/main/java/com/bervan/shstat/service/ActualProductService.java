package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.ActualProduct;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.repository.ActualProductsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.bervan.shstat.service.ProductService.productPerDateAttributeProperties;

@Slf4j
@Service
public class ActualProductService {
    private static final Integer currentDateOffsetInDays = 2; //is ok, good offers will not last forever!
    private static final int BATCH_SIZE = 1000;
    private final ActualProductsRepository actualProductsRepository;
    private final Map<Long, ActualProduct> delayedToBeSaved = new HashMap<>();
    private final Map<Long, Set<Date>> inMemoryData = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${product-update.delayed-save}")
    private boolean delayedSave = true;

    public ActualProductService(ActualProductsRepository actualProductsRepository) {
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateActualProducts(Object date, Product mappedProduct, User commonUser) {
        Date scrapDate = (Date) productPerDateAttributeProperties.stream()
                .filter(e -> e.attr.equals("Date"))
                .findFirst()
                .get().mapper.map(date);

        Long productId = mappedProduct.getId();

        lock.lock();
        try {
            Set<Date> knownDates = inMemoryData.computeIfAbsent(productId, k -> new HashSet<>());
            if (knownDates.contains(scrapDate)) {
                return;
            }

            List<ProductBasedOnDateAttributes> sortedPrices = mappedProduct.getProductBasedOnDateAttributes().stream()
                    .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                    .toList();

            ActualProduct ap = actualProductsRepository.findByProductId(productId)
                    .map(existing -> {
                        if (existing.getScrapDate().before(scrapDate)) {
                            existing.setScrapDate(scrapDate);
                            existing.setProductId(productId);
                            existing.setProductName(mappedProduct.getName());
                            existing.setShop(mappedProduct.getShop());
                            existing.setPrice(sortedPrices.get(0).getPrice());
                            if (!existing.getOwners().contains(commonUser)) {
                                existing.addOwner(commonUser);
                            }
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        ActualProduct newAP = new ActualProduct();
                        newAP.addOwner(commonUser);
                        newAP.setProductId(productId);
                        newAP.setProductName(mappedProduct.getName());
                        newAP.setShop(mappedProduct.getShop());
                        newAP.setPrice(sortedPrices.get(0).getPrice());
                        newAP.setScrapDate(scrapDate);
                        return newAP;
                    });

            delayedToBeSaved.put(productId, ap);
            knownDates.add(scrapDate);
        } finally {
            lock.unlock();
        }

        if (!delayedSave) {
            flushActualProductsToDb();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void flushActualProductsToDb() {
        try {
            if (lock.tryLock(5, TimeUnit.MINUTES)) {
                log.info("flushActualProductsToDb started!");
                try {
                    if (!delayedToBeSaved.isEmpty()) {
                        List<ActualProduct> batch = new ArrayList<>(delayedToBeSaved.values());
                        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, batch.size());
                            actualProductsRepository.saveAll(batch.subList(i, end));
                        }
                        delayedToBeSaved.clear();
                        inMemoryData.clear();
                    }
                    log.info("flushActualProductsToDb ended!");
                } catch (Exception e) {
                    log.error("Failed to flush actual products", e);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Flush skipped due to active write lock");
            }
        } catch (InterruptedException e) {
            log.error("flushActualProductsToDb InterruptedException for write lock");
        }
    }

    @Scheduled(cron = "0 15 0 * * *")
    public void deleteActualProducts() {
        try {
            actualProductsRepository.deleteRelatedProductOwners(currentDateOffsetInDays);
            actualProductsRepository.deleteNotActualProducts(currentDateOffsetInDays);
        } catch (Exception e) {
            log.error("Failed to updateActualProducts!", e);
        }
    }

    public long count() {
        return actualProductsRepository.count();
    }

    public Page<ActualProduct> findAll(Pageable pageable) {
        return actualProductsRepository.findAll(pageable);
    }
}
