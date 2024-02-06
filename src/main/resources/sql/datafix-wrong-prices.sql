SELECT *
FROM product_based_on_date_attributes pda
WHERE pda.price >= 90000; #verify first
DELETE
FROM product_based_on_date_attributes
WHERE price >= 90000;

#can take hours:
# SELECT pda.product_id, pda.id, pda.price, pda.scrap_date
DELETE FROM product_based_on_date_attributes pda
WHERE price >= 5 * (SELECT AVG(pda1.price)
                   FROM product_based_on_date_attributes pda1
                   WHERE pda1.product_id = pda.product_id
                     AND pda1.price > 0);
DELETE
FROM product_stats
WHERE 1 = 1



