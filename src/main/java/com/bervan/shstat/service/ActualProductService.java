package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.ActualProduct;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.repository.ActualProductsRepository;
import lombok.extern.slf4j.Slf4j;
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
    private final List<ActualProduct> delayedToBeSaved = new LinkedList<>();
    private final Map<Long, Set<Date>> inMemoryData = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ActualProductService(ActualProductsRepository actualProductsRepository) {
        this.actualProductsRepository = actualProductsRepository;
    }

    public void updateActualProducts(Object date, Product mappedProduct, User commonUser) {
        Date scrapDate = (Date) productPerDateAttributeProperties.stream()
                .filter(e -> e.attr.equals("Date")).findFirst()
                .get().mapper.map(date);

        Long productId = mappedProduct.getId();
        Optional<ActualProduct> actualProduct = actualProductsRepository.findByProductId(productId);

        lock.lock();
        try {
            if (inMemoryData.containsKey(productId)) {
                if (inMemoryData.get(productId).contains(scrapDate)) {
                    return;
                }
            } else {
                inMemoryData.put(productId, new HashSet<>());
            }

            if (actualProduct.isPresent()) {
                ActualProduct ap = actualProduct.get();
                if (ap.getScrapDate().before(scrapDate)) {
                    ap.setScrapDate(scrapDate);
                    if (!ap.getOwners().contains(commonUser)) {
                        ap.addOwner(commonUser);
                    }
                    delayedToBeSaved.add(ap);
                }
            } else {
                ActualProduct newAP = new ActualProduct();
                newAP.addOwner(commonUser);
                newAP.setProductId(productId);
                newAP.setScrapDate(scrapDate);
                delayedToBeSaved.add(newAP);
            }

            inMemoryData.get(productId).add(scrapDate);
        } finally {
            lock.unlock();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void flushActualProductsToDb() {
        try {
            if (lock.tryLock(5, TimeUnit.MINUTES)) {
                try {
                    if (!delayedToBeSaved.isEmpty()) {
                        for (int i = 0; i < delayedToBeSaved.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, delayedToBeSaved.size());
                            actualProductsRepository.saveAll(delayedToBeSaved.subList(i, end));
                        }
                        delayedToBeSaved.clear();
                        inMemoryData.clear();
                    }
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
}
