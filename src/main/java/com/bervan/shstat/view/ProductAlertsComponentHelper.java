package com.bervan.shstat.view;

import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.CommonComponentHelper;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ShopConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductAlertsComponentHelper extends CommonComponentHelper<Long, ProductAlert> {
    private final List<String> allAvailableCategories;
    public ProductAlertsComponentHelper(List<String> allAvailableCategories) {
        super(ProductAlert.class);
        this.allAvailableCategories = allAvailableCategories;
    }


    @Override
    protected List<String> getAllValuesForDynamicDropdowns(String key, ProductAlert item) {
        return new ArrayList<>();
    }

    @Override
    protected List<String> getAllValuesForDynamicMultiDropdowns(String key, ProductAlert item) {
        if (key.equals("productCategories")) {
            return allAvailableCategories.stream().sorted(String::compareTo).toList();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<String> getInitialSelectedValueForDynamicMultiDropdown(String key, ProductAlert item) {
        if (item != null && key.equals("productCategories")) {
            return item.getProductCategories();
        } else if (item != null && key.equals("emails")) {
            return item.getEmails();
        }
        return new ArrayList<>();
    }

    @Override
    protected String getInitialSelectedValueForDynamicDropdown(String key, ProductAlert item) {
        return null;
    }

}
