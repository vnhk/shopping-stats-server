package com.shstat.favorites;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    @PersistenceContext
    private EntityManager entityManager;
    private final FavoritesListRepository favoritesListRepository;
    private final FavoriteProductsRepository favoriteProductsRepository;

    public FavoriteService(FavoritesListRepository favoritesListRepository,
                           FavoriteProductsRepository favoriteProductsRepository) {
        this.favoritesListRepository = favoritesListRepository;
        this.favoriteProductsRepository = favoriteProductsRepository;
    }

    public void refreshTableForFavorites() {
        favoriteProductsRepository.deleteAll();
        List<FavoritesList> lists = favoritesListRepository.findAll();
        for (FavoritesList list : lists) {
            if (list.isDisabled()) {
                continue;
            }
            Set<FavoritesRule> rules = list.getFavoritesRules();
            boolean firstRule = true;
            Set<FavoriteProduct> intersectSetForRules = new HashSet<>();
            for (FavoritesRule rule : rules) {
                if (rule.isDisabled()) {
                    continue;
                }
                Set<FavoriteProduct> toBeRemoved = new HashSet<>();
                String productName = rule.getProductName();
                String productNameSQL = buildProductNameSQL(productName);

                String sql = "   WITH RankedPrices AS (SELECT DISTINCT product_id, AVG(price) AS average_price FROM scrapdb.product_based_on_date_attributes AS pda " +
                        " WHERE price <> -1 AND MONTH(pda.scrap_date) > MONTH(CURRENT_DATE - INTERVAL 3 MONTH) GROUP BY product_id)" +
                        " SELECT DISTINCT p.id as product_id, p.name as product_name, p.shop, pc.categories as category, pda.price," +
                        " :listName as list_name, rp.average_price as avg_price, p.img_src, pda.scrap_date, ptav.value as offer_url, " +
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
                        (
                                rule.isOnlyActive() ?
                                        "            AND scrap_date >= DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
                                                "    AND scrap_date < CURDATE()" : ""
                        ) +
                        " AND pda.price <> -1 " +
                        (
                                rule.getCategory() == null ? "" :
                                        " AND pc.categories in :category "
                        ) +
                        (
                                rule.getShop() == null ? "" :
                                        " AND p.shop in :shop "
                        ) +
                        (
                                rule.getPriceMin() == null ? "" :
                                        " AND pda.price >= :priceMin "
                        ) +
                        (
                                rule.getPriceMax() == null ? "" :
                                        " AND pda.price <= :priceMax "
                        )
                        +
                        " AND p.id = COALESCE(:productId, p.id) " +
                        " AND ( " + productNameSQL + " )";

                Query query = entityManager.createNativeQuery(sql)
                        .setParameter("listName", list.getListName())
                        .setParameter("productId", rule.getProductId());
                if (rule.getCategory() != null) {
                    query = query.setParameter("category", getSplit(rule.getCategory()));
                }
                if (rule.getShop() != null) {
                    query = query.setParameter("shop", getSplit(rule.getShop()));
                }
                if (rule.getPriceMin() != null) {
                    query = query.setParameter("priceMin", rule.getPriceMin());
                }
                if (rule.getPriceMax() != null) {
                    query = query.setParameter("priceMax", rule.getPriceMax());
                }

                if (productName != null) {
                    String[] productNames = productName.split(";");
                    int names = productNames.length;

                    for (int i = 0; i < names; i++) {
                        query = query.setParameter("name" + i, productNames[i]);
                    }
                }

                Set<FavoriteProduct> resultList = map(query.getResultList());
                if (firstRule) {
                    intersectSetForRules.addAll(resultList);
                } else {
                    for (FavoriteProduct o : intersectSetForRules) {
                        if (!resultList.contains(o)) {
                            toBeRemoved.add(o);
                        }
                    }

                    intersectSetForRules.removeAll(toBeRemoved);
                }
                firstRule = false;
            }
            List<FavoriteProduct> favoriteProducts = favoriteProductsRepository.saveAll(intersectSetForRules);
        }
    }

    private List<String> getSplit(String param) {
        return Arrays.stream(param.split(";")).collect(Collectors.toList());
    }

    private String buildProductNameSQL(String productName) {
        if (productName == null) {
            return "1=1";
        }

        String[] productNames = productName.split(";");
        String format = " UPPER(p.name) LIKE UPPER(COALESCE(:%s, p.name)) ";
        StringBuilder res = new StringBuilder();

        int i = 0;
        for (; i < productNames.length - 1; i++) {
            res.append(String.format(format, "name" + i)).append(" OR ");
        }

        return (res + String.format(format, "name" + i)).trim();
    }

    private Set<FavoriteProduct> map(List<Object[]> res) {
        Set<FavoriteProduct> favoriteProducts = new HashSet<>();
        for (Object[] item : res) {
            FavoriteProduct favoriteProduct = new FavoriteProduct(null, (Long) item[0],
                    (String) item[1], (String) item[2], (String) item[3], (BigDecimal) item[4],
                    (String) item[5], (BigDecimal) item[6], (String) item[7], (Date) item[8], (String) item[9], ((BigDecimal) item[10]));
            favoriteProducts.add(favoriteProduct);
        }

        return favoriteProducts;
    }


    public Page<FavoriteProduct> getFavorites(Pageable pageable, String favoritesListName, String shop, String category) {
        return favoritesListRepository.findFavorites(favoritesListName, shop, category, pageable);
    }

    public Set<String> getLists() {
        return favoritesListRepository.findAll().stream().map(FavoritesList::getListName).collect(Collectors.toSet());
    }
}
