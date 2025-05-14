package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapAuditRepository extends BaseRepository<ScrapAudit, Long> {
}
