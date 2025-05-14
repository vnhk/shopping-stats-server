package com.bervan.shstat;

import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.queue.QueueService;
import com.bervan.shstat.repository.ProductConfigRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class ShopSchedulerTasks {
    private final BervanLogger log;
    private final ProductConfigRepository productConfigRepository;
    private final QueueService queueService;

    public ShopSchedulerTasks(BervanLogger log, ProductConfigRepository productConfigRepository, QueueService queueService) {
        this.log = log;
        this.productConfigRepository = productConfigRepository;
        this.queueService = queueService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scrapAddToQueue() throws InterruptedException {
        try {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
//            SearchRequest request = new SearchRequest();
//            request.addCriterion("HOUR_CRITERION", Operator.AND_OPERATOR, ProductConfig.class,
//                    "scrapTime", SearchOperation.EQUALS_OPERATION, LocalTime.of(hour, 0));
//            request.setAddOwnerCriterion(false);
//            Set<ProductConfig> productConfigToProcess = productConfigService.load(request, Pageable.ofSize(500));

            Set<ProductConfig> productConfigToProcess = productConfigRepository.findAllActiveForHour(LocalTime.of(hour, 0));

            for (ProductConfig configToProcess : productConfigToProcess) {
                ConfigRoot config = new ConfigRoot();
                config.setShopName(configToProcess.getShop().getShopName());
                config.setBaseUrl(configToProcess.getShop().getBaseUrl());
                ConfigProduct configProduct = new ConfigProduct();
                configProduct.setName(configToProcess.getName());
                ScrapTime scrapTime = new ScrapTime();
                scrapTime.setHours(configToProcess.getScrapTime().getHour());
                configProduct.setScrapTime(scrapTime);
                configProduct.setUrl(configToProcess.getUrl());
                configProduct.setCategories(new HashSet<>(configToProcess.getCategories()));
                ArrayList<ConfigProduct> products = new ArrayList<>();
                products.add(configProduct);
                config.setProducts(products);
                ScrapContext context = new ScrapContext();
                context.setRoot(config);
                context.setProduct(configProduct);
                context.setScrapDate(new Date());

                log.info("Adding scrap request to queue: " + configToProcess.getName() + " from "
                        + configToProcess.getShop().getShopName());
                queueService.addScrapingToQueue(context);
            }


        } catch (Exception e) {
            log.error("scrapAddToQueue: FAILED!", e);
        }
    }
}
