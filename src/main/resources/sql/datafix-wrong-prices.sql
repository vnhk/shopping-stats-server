SELECT * FROM scrapdb.product_based_on_date_attributes pda WHERE pda.price >= 90000; #verify first
DELETE FROM scrapdb.product_based_on_date_attributes WHERE price >= 90000;
DELETE FROM product_stats WHERE 1=1



