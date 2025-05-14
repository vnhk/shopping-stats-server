package com.bervan.shstat;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import org.springframework.stereotype.Service;

@Service
public class ScrapAuditService extends BaseService<Long, ScrapAudit> {
    protected ScrapAuditService(BaseRepository<ScrapAudit, Long> repository, SearchService searchService) {
        super(repository, searchService);
    }
}
