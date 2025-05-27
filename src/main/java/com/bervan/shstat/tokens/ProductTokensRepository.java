package com.bervan.shstat.tokens;

import com.bervan.history.model.BaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductTokensRepository extends BaseRepository<ProductTokens, Long> {
    @Query("""
                SELECT
                    pt.value
                FROM
                    ProductTokens pt
                WHERE
                    pt.productId = :id
            """)
    Set<String> findByProductId(Long id);

    @Query("""
                SELECT
                    pt.productId,
                    SUM(pt.factor) AS score,
                    COUNT(pt) AS matchedTokens
                FROM
                    ProductTokens pt
                WHERE
                    pt.value IN :tokens
                GROUP BY
                    pt.productId
                ORDER BY
                    score DESC, matchedTokens DESC
            """)
    List<Object[]> findByTokens(@Param("tokens") Set<String> tokens, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM product_tokens_owners
            WHERE product_tokens_id IN (
                SELECT id from product_tokens WHERE product_id = :productId
            )
            """, nativeQuery = true)
    void deleteOwnersByProductId(Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductTokens p WHERE p.productId = :productId")
    void deleteByProductId(Long productId);

}
