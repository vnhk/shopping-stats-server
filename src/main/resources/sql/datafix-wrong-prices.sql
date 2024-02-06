SELECT *
FROM product_based_on_date_attributes pda
WHERE pda.price >= 90000; #verify first
DELETE
FROM product_based_on_date_attributes
WHERE price >= 90000;

#can take hours:
#do backup first
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
             WHERE pda.price >= 5 * (SELECT AVG(pda1.price)
                                     FROM product_based_on_date_attributes pda1
                                     WHERE pda1.product_id = pda.product_id
                                       AND pda1.price > 0));
DELETE
FROM product_stats
WHERE 1 = 1



