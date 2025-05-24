package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.bervan.shstat.repository.ScrapAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class ScrapAuditService extends BaseService<Long, ScrapAudit> {
    protected ScrapAuditService(ScrapAuditRepository repository, SearchService searchService) {
        super(repository, searchService);
    }

    public void updateSavedProductsCount(String shop, String productListName, String productListUrl, int size) {
        Optional<ScrapAudit> scrapAudit = ((ScrapAuditRepository) repository)
                .findByProductConfigAndDate(shop, productListName, productListUrl, LocalDate.now());
        if (scrapAudit.isPresent()) {
            scrapAudit.get().addToSavedProducts(size);
            repository.save(scrapAudit.get());
            log.info("ScrapAudit - processed products - updated");
        } else {
            log.error("ScrapAudit not found for the given date and product config! {} | {} | {}", shop, productListName, productListUrl);
        }
    }
}
