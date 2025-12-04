package com.bervan.shstat;

import com.bervan.common.user.UserRepository;
import com.bervan.logging.JsonLogger;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.bervan.shstat.queue.QueueService;
import com.bervan.shstat.repository.ProductConfigRepository;
import com.bervan.shstat.repository.ScrapAuditRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class ShopSchedulerTasks {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");

    private final ProductConfigRepository productConfigRepository;
    private final ScrapAuditRepository scrapAuditRepository;
    private final QueueService queueService;
    private final UserRepository userRepository;

    public ShopSchedulerTasks(
            ProductConfigRepository productConfigRepository,
            ScrapAuditRepository scrapAuditRepository,
            QueueService queueService, UserRepository userRepository) {

        this.productConfigRepository = productConfigRepository;
        this.scrapAuditRepository = scrapAuditRepository;
        this.queueService = queueService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void scrapAddToQueue() throws InterruptedException {
        try {
            LocalTime now = LocalTime.now();
            LocalDate localDate = LocalDate.now();
            Set<ProductConfig> productConfigToProcess = productConfigRepository.findAllActiveForHour(now, localDate);

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
                configProduct.setMinPrice(configToProcess.getMinPrice());
                configProduct.setMaxPrice(configToProcess.getMaxPrice());
                configProduct.setCategories(new HashSet<>(configToProcess.getCategories()));
                ArrayList<ConfigProduct> products = new ArrayList<>();
                products.add(configProduct);
                config.setProducts(products);
                ScrapContext context = new ScrapContext();
                context.setRoot(config);
                context.setProduct(configProduct);
                context.setScrapDate(new Date());

                queueService.addScrapingToQueue(context);
                log.info("Added scrap request to queue: " + configToProcess.getName() + " from "
                        + configToProcess.getShop().getShopName());
                ScrapAudit scrapAudit = new ScrapAudit();
                scrapAudit.setDeleted(false);
                scrapAudit.setProductConfig(configToProcess);
                scrapAudit.setDate(localDate);
                scrapAudit.addOwner(userRepository.findByUsername("COMMON_USER").get());
                scrapAuditRepository.save(scrapAudit);
            }

        } catch (Exception e) {
            log.error("scrapAddToQueue: FAILED!", e);
        }
    }
}
