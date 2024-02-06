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
#faster but works only for actual_products:
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.price >= 5 * st.avg_whole_history);

DELETE
FROM product_stats
WHERE 1 = 1



