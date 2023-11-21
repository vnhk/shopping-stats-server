package com.shstat.repository;

import com.shstat.entity.Product;
import com.shstat.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface ProductAttributeRepository<T extends ProductAttribute> extends JpaRepository<T, Long> {
    Optional<? extends ProductAttribute> findByProductAndNameAndValue(Product product, String name, Object value);

    Optional<? extends ProductAttribute> findByProductAndName(Product product, String name);
}
