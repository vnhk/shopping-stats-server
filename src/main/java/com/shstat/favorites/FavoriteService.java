package com.shstat.favorites;

import com.shstat.ProductService;
import com.shstat.response.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    @PersistenceContext
    private EntityManager entityManager;
    private final FavoritesListRepository favoritesListRepository;
    private final ProductService productService;

    public FavoriteService(FavoritesListRepository favoritesListRepository, ProductService productService) {
        this.favoritesListRepository = favoritesListRepository;
        this.productService = productService;
    }

    @Transactional
    public ApiResponse refreshTableForFavorites() {
        String createTableQuery = "CREATE OR REPLACE TABLE FAVORITES_TABLE_VIEW AS ";
        String insertIntoQuery = "INSERT INTO FAVORITES_TABLE_VIEW ";

        List<FavoritesList> lists = favoritesListRepository.findAll();
        for (FavoritesList list : lists) {
            Set<FavoritesRule> rules = list.getFavoritesRules();
            //more than one rule will be connected with AND or OR but hardcoded...
            //in the future
            //now - only first rule is supported
            String sql = "";
            for (FavoritesRule rule : rules) {
                sql = "   WITH RankedPrices AS (SELECT DISTINCT product_id, AVG(price) AS average_price FROM scrapdb.product_based_on_date_attributes AS pda " +
                        " WHERE price <> -1 AND MONTH(pda.scrap_date) > MONTH(CURRENT_DATE - INTERVAL 3 MONTH) GROUP BY product_id)" +
                        " SELECT DISTINCT p.id as product_id, p.name as product_name, p.shop, pc.categories as category, pda.price," +
                        " :listName as list_name, rp.average_price as avg_price, p.img_src as img_src, pda.scrap_date, ptav.value as offer_url, " +
                        " (IF(pda.price >= rp.average_price, 0, (1 - pda.price / rp.average_price) * 100)) as discount_in_percent " +
                        " FROM scrapdb.product p " +
                        " JOIN RankedPrices rp on p.id = rp.product_id " +
                        " JOIN scrapdb.product_categories pc on p.id = pc.product_id " +
                        " JOIN scrapdb.product_based_on_date_attributes pda on p.id = pda.product_id " +
                        " JOIN scrapdb.product_list_text_attribute pta on p.id = pta.product_id " +
                        " JOIN scrapdb.product_list_text_attribute_value ptav on pta.id = ptav.product_list_text_attribute_id " +
                        " WHERE pta.name = 'Offer Url' AND pda.scrap_date = (SELECT MAX(scrap_date) " +
                        "            FROM scrapdb.product_based_on_date_attributes AS pda1 " +
                        "            WHERE price <> -1 " +
                        "            AND pda.id = pda1.id) " +
                        (rule.isOnlyActive() ?
                                "            AND scrap_date >= DATE_SUB(CURDATE(), INTERVAL 2 DAY) " +
                                        "    AND scrap_date < CURDATE()" : "") +
                        " AND pda.price <> -1 " +
                        " AND pc.categories = COALESCE(:category, pc.categories) " +
                        " AND p.shop = COALESCE(:shop, p.shop) " +
                        " AND p.id = COALESCE(:productId, p.id) " +
                        " AND UPPER(p.name) LIKE UPPER(COALESCE(:productName, p.name)) ";

                entityManager.createNativeQuery(createTableQuery + sql)
                        .setParameter("listName", list.getListName())
                        .setParameter("category", rule.getCategory())
                        .setParameter("shop", rule.getShop())
                        .setParameter("productId", rule.getProductId())
                        .setParameter("productName", rule.getProductName())
                        .executeUpdate();
                break;
            }
            //now - only first list is supported...
            //todo - support for many lists
            break;
        }

        return new ApiResponse(Collections.singletonList("Views refreshed."));
    }


    public Page<FavoritesListRepository.ProductProjection> getFavorites(Pageable pageable, String favoritesListName, String shop, String category) {
        return favoritesListRepository.findFavorites(favoritesListName, shop, category, pageable);
    }

    public Set<String> getLists() {
        return favoritesListRepository.findAll().stream().map(FavoritesList::getListName).collect(Collectors.toSet());
    }
}
