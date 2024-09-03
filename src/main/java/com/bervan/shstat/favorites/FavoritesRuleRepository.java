package com.bervan.shstat.favorites;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritesRuleRepository extends JpaRepository<FavoritesRule, Long> {

}
