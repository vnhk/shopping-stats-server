DELETE FROM scrapdb.actual_product WHERE 1=1;

INSERT INTO scrapdb.actual_product (id, product_id, scrap_date) (SELECT NEXTVAL(scrapdb.actual_product_seq), pda.product_id, pda.scrap_date
                                    FROM scrapdb.product_based_on_date_attributes pda
                                    WHERE pda.scrap_date IN (SELECT MAX(pdaIn.scrap_date)
                                                             FROM scrapdb.product_based_on_date_attributes pdaIn
                                                             WHERE pdaIn.scrap_date >= DATE_SUB(CURDATE(), INTERVAL :offset DAY)
                                                               AND pdaIn.scrap_date < CURDATE()
                                                               AND pda.product_id = pdaIn.product_id)
                                      AND pda.price > 0)



