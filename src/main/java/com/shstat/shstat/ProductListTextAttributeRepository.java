package com.shstat.shstat;

import com.shstat.shstat.entity.Product;
import com.shstat.shstat.entity.ProductListTextAttribute;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductListTextAttributeRepository extends ProductAttributeRepository<ProductListTextAttribute> {
}
