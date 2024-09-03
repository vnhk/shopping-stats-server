package com.bervan.shstat.repository;

import com.bervan.history.model.BaseRepository;
import com.bervan.shstat.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

public interface ProductAttributeRepository<T extends ProductAttribute> extends BaseRepository<T, Long> {
}
