package com.shstat.shstat;

import com.shstat.shstat.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndShopAndProductListNameAndProductListUrl(String name,
                                                                           String shop,
                                                                           String productListName,
                                                                           String productListUrl);
}
