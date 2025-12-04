package com.bervan.shstat.service;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.logging.BaseProcessContext;
import com.bervan.logging.JsonLogger;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.bervan.shstat.repository.ScrapAuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScrapAuditService extends BaseService<Long, ScrapAudit> {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");
    private final Map<String, ScrapAudit> scrapAuditCache = new ConcurrentHashMap<>();

    protected ScrapAuditService(ScrapAuditRepository repository, SearchService searchService) {
        super(repository, searchService);
    }

    public synchronized void updateSavedProductsCount(String shop, String productListName, String productListUrl, int size, BaseProcessContext addProductsContext) {
        String key = shop + "|" + productListName + "|" + productListUrl + "|" + LocalDate.now();

        ScrapAudit audit = scrapAuditCache.computeIfAbsent(key, k -> {
            Optional<ScrapAudit> existing = ((ScrapAuditRepository) repository)
                    .findByProductConfigAndDate(shop, productListName, productListUrl, LocalDate.now());
            if (existing.isPresent()) {
                return existing.get();
            } else {
                log.error(addProductsContext.map(), "ScrapAudit not found for the given date and product config! {} | {} | {}", shop, productListName, productListUrl);
                return null;
            }
        });

        if (audit != null) {
            audit.addToSavedProducts(size);
            repository.save(audit);
        }
    }
}
