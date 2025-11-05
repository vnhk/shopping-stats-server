package com.bervan.shstat.service;

import com.bervan.common.user.User;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.entity.ProductStats;
import com.bervan.shstat.repository.ProductBestOfferRepository;
import com.bervan.shstat.repository.ProductStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class ProductStatsService {
    private static final int BATCH_SIZE = 1000;
    private final ProductStatsRepository productStatsRepository;
    private final ProductBestOfferRepository productBestOfferRepository;
    private final Map<Long, ProductStats> delayedToBeSaved = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${product-update.delayed-save}")
    private boolean delayedSave = true;

    public ProductStatsService(ProductStatsRepository productStatsRepository, ProductBestOfferRepository productBestOfferRepository) {
        this.productStatsRepository = productStatsRepository;
        this.productBestOfferRepository = productBestOfferRepository;
    }

    public static BigDecimal calculateAvgForMonthsInMemory(List<ProductBasedOnDateAttributes> attributes, int monthOffset) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusMonths(monthOffset);
        BigDecimal total = BigDecimal.ZERO;
        long totalDays = 0;

        for (ProductBasedOnDateAttributes attr : attributes) {
            if (attr.getPrice().compareTo(BigDecimal.ZERO) <= 0) continue;
            if (Boolean.TRUE.equals(attr.getDeleted())) continue;

            LocalDate start = toLocalDate(attr.getScrapDate());
            LocalDate end = Optional.ofNullable(attr.getScrapDateEnd())
                    .map(ProductStatsService::toLocalDate)
                    .orElse(today);
            if (end.isAfter(today)) end = today;

            if (end.isBefore(fromDate)) continue;

            LocalDate effectiveStart = start.isBefore(fromDate) ? fromDate : start;
            long days = ChronoUnit.DAYS.between(effectiveStart, end);
            if (days <= 0) continue;

            total = total.add(attr.getPrice().multiply(BigDecimal.valueOf(days)));
            totalDays += days;
        }

        return totalDays > 0 ? total.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void updateProductStats(Product mappedProduct, User commonUser) {
        if (mappedProduct.getProductBasedOnDateAttributes() == null || mappedProduct.getProductBasedOnDateAttributes().stream().filter(e -> !e.isDeleted()).count() < 2) {
            log.warn("updateProductStats - No sense to create stats because there is no enough historical data for product: {} id", mappedProduct.getId());
            return;
        }

        Long productId = mappedProduct.getId();
        Optional<ProductStats> byProductId = productStatsRepository.findByProductId(productId);
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(productId);
            byProductId = Optional.of(productStats);
        }

        if (byProductId.get().getOwners().isEmpty()) {
            byProductId.get().addOwner(commonUser);
        }

        updateStats(byProductId, mappedProduct.getProductBasedOnDateAttributes());
        lock.lock();
        try {
            delayedToBeSaved.put(productId, byProductId.get());
        } finally {
            lock.unlock();
        }

        if (!delayedSave) {
            flushStatsToDb();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void flushStatsToDb() {
        try {
            if (lock.tryLock(5, TimeUnit.MINUTES)) {
                log.info("flushStatsToDb started!");
                try {
                    if (!delayedToBeSaved.isEmpty()) {
                        List<ProductStats> batch = new ArrayList<>(delayedToBeSaved.values());
                        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, batch.size());
                            productStatsRepository.saveAll(batch.subList(i, end));
                        }
                        delayedToBeSaved.clear();
                    }
                    log.info("flushStatsToDb ended!");
                } catch (Exception e) {
                    log.error("Failed to flush stats", e);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Flush skipped due to active write lock");
            }
        } catch (InterruptedException e) {
            log.error("flushStatsToDb InterruptedException for write lock");
        }
    }

    public void updateStatsAndSave(Optional<ProductStats> byProductId, Product product) {
        if (byProductId.isEmpty()) {
            ProductStats productStats = new ProductStats();
            productStats.setProductId(product.getId());
            byProductId = Optional.of(productStats);
        }
        updateStats(byProductId, product.getProductBasedOnDateAttributes());
        productStatsRepository.save(byProductId.get());
    }

    private void updateStats(Optional<ProductStats> byProductId, List<ProductBasedOnDateAttributes> productBasedOnDateAttributes) {
        List<ProductBasedOnDateAttributes> sortedPrices = productBasedOnDateAttributes.stream()
                .sorted(Comparator.comparing(ProductBasedOnDateAttributes::getScrapDate).reversed())
                .toList();
        updateHistoricalLow(byProductId.get(), sortedPrices);
        updateAvgWholeHistory(byProductId.get(), sortedPrices);
        updateAvgLastXMonth(byProductId.get(), sortedPrices, 1);
        updateAvgLastXMonth(byProductId.get(), sortedPrices, 2);
        updateAvgLastXMonth(byProductId.get(), sortedPrices, 3);
        updateAvgLastXMonth(byProductId.get(), sortedPrices, 6);
        updateAvgLastXMonth(byProductId.get(), sortedPrices, 12);
    }

    private void updateHistoricalLow(ProductStats productStats, List<ProductBasedOnDateAttributes> sortedPrices) {
        OptionalDouble min = sortedPrices.stream().filter(e -> !e.isDeleted()).mapToDouble(e -> e.getPrice().doubleValue())
                .min();
        if (min.isPresent()) {
            productStats.setHistoricalLow(BigDecimal.valueOf(min.getAsDouble()));
        }
    }

    private void updateAvgWholeHistory(ProductStats productStats, List<ProductBasedOnDateAttributes> sortedPrices) {
        BigDecimal avg = calculateAvgForMonthsInMemory(sortedPrices, 12000);
        productStats.setAvgWholeHistory(avg);
    }

    private void updateAvgLastXMonth(ProductStats productStats, List<ProductBasedOnDateAttributes> sortedPrices, int months) {
        BigDecimal avg = calculateAvgForMonthsInMemory(sortedPrices, months);
        switch (months) {
            case 1 -> productStats.setAvg1Month(avg);
            case 2 -> productStats.setAvg2Month(avg);
            case 3 -> productStats.setAvg3Month(avg);
            case 6 -> productStats.setAvg6Month(avg);
            case 12 -> productStats.setAvg12Month(avg);
            default -> throw new IllegalArgumentException("Unsupported month range: " + months);
        }
    }

    public Optional<ProductStats> findByProductId(Long id) {
        return productStatsRepository.findByProductId(id);
    }

    public List<ProductStats> findAllByProductId(Collection<Long> productIds) {
        return productStatsRepository.findAllByProductIdIn(productIds);
    }
}
