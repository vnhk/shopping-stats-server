SELECT *
FROM product_based_on_date_attributes pda
WHERE pda.price >= 90000; #verify first
DELETE
FROM product_based_on_date_attributes
WHERE price >= 90000;


#the purpose of the following queries is to identify and delete wrong prices by comparing them to the avg price in given range:
#always starts with the given order

#can take hours:
#do backup first
# DELETE
# FROM product_based_on_date_attributes
# WHERE id in (SELECT pda.id
#              FROM product_based_on_date_attributes pda
#              WHERE pda.price >= 5 * (SELECT AVG(pda1.price)
#                                      FROM product_based_on_date_attributes pda1
#                                      WHERE pda1.product_id = pda.product_id
#                                        AND pda1.price > 0));
#faster but works only for actual_products:
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
                      JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.price >= 5 * st.avg_whole_history);

#compared to last 12 month
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
                      JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 12 MONTH)
               AND pda.price >= 5 * st.avg12month);


#compared to last 6 month avg
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
                      JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 6 MONTH)
               AND pda.price >= 5 * st.avg6month);


#compared to last 3 month avg
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
                      JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 3 MONTH)
               AND pda.price >= 5 * st.avg3month);


#compared to last 2 month avg
DELETE
FROM product_based_on_date_attributes
WHERE id in (SELECT pda.id
             FROM product_based_on_date_attributes pda
                      JOIN product_stats st on st.product_id = pda.product_id
             WHERE pda.scrap_date >= DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 2 MONTH)
               AND pda.price >= 5 * st.avg2month);

DELETE
FROM product_stats
WHERE 1 = 1



