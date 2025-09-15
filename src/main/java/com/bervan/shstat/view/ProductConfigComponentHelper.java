package com.bervan.shstat.view;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.CommonComponentHelper;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ShopConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductConfigComponentHelper extends CommonComponentHelper<Long, ProductConfig> {
    private final Map<String, ShopConfig> shops;
    private final List<String> allAvailableCategories;
    private final Function<ProductConfig, List<String>> loadCategories;

    public ProductConfigComponentHelper(Map<String, ShopConfig> shops, List<String> allAvailableCategories, Function<ProductConfig, List<String>> loadCategories) {
        super(ProductConfig.class);
        this.shops = shops;
        this.allAvailableCategories = allAvailableCategories;
        this.loadCategories = loadCategories;
    }

    @Override
    protected List<String> getAllValuesForDynamicDropdowns(String key, ProductConfig item) {
        if (key.equals("shop")) {
            return shops.keySet().stream().toList();
        }

        return new ArrayList<>();
    }

    @Override
    protected List<String> getAllValuesForDynamicMultiDropdowns(String key, ProductConfig item) {
        if (key.equals("categories")) {
            return allAvailableCategories.stream().sorted(String::compareTo).toList();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<String> getInitialSelectedValueForDynamicMultiDropdown(String key, ProductConfig item) {
        if (key.equals("categories") && item != null) {
            List<String> categories = item.getCategories();
            if (categories == null) {
                //try load from db
                return loadCategories.apply(item);
            }
            return categories;
        }
        return new ArrayList<>();
    }

    @Override
    protected String getInitialSelectedValueForDynamicDropdown(String key, ProductConfig item) {
        //not used, because shop is visible in table
        if (key.equals("shop") && item != null && item.getShop() != null) {
            return item.getShop().getShopName();
        }

        return null;
    }

    @Override
    public Object getFieldValueForNewItemDialog(Map.Entry<Field, AutoConfigurableField> fieldAutoConfigurableFieldEntry) {
        Object fieldValueForNewItemDialog = super.getFieldValueForNewItemDialog(fieldAutoConfigurableFieldEntry);
        //convert shop name dropdown selected value to ShopConfig
        if (fieldAutoConfigurableFieldEntry.getKey().getName().equals("shop")) {
            String strValue = (String) fieldValueForNewItemDialog;
            return shops.get(strValue);
        }
        return fieldValueForNewItemDialog;
    }

}
