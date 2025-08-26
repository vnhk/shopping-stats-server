package com.bervan.shstat.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.impl.JreJsonArray;
import elemental.json.impl.JreJsonFactory;
import elemental.json.impl.JreJsonObject;

import java.util.ArrayList;
import java.util.List;

@JsModule("./price-chart-component.js")
@Tag("canvas")
public class ProductPriceChart extends Component implements HasSize {
    public ProductPriceChart(List<String> labels, List<Double> prices, Double avg) {
        setId("priceChart");

        // Prepare avg for chart, add N times avg for drawing a line
        List<Double> avgData = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            avgData.add(avg);
        }

        // Initialize the chart with labels and prices passed from Java
        JreJsonObject labelsJson = getJreJsonObject(labels);
        JreJsonObject pricesJson = getJreJsonObject(prices);
        JreJsonObject avgDataJson = getJreJsonObject(avgData);

        UI.getCurrent().getPage().executeJs(
                "window.renderPriceChart($0, $1, $2, $3)",
                getElement(),
                labelsJson.get("data"),
                pricesJson.get("data"),
                avgDataJson.get("data")
        );
    }

    // Converts a List into a JreJsonObject with a single "data" key pointing to a JreJsonArray
    private static JreJsonObject getJreJsonObject(List<?> labels) {
        JreJsonObject jreJsonObject = new JreJsonObject(new JreJsonFactory());
        JreJsonArray jreJsonArray = new JreJsonArray(new JreJsonFactory());

        // Fill the array with stringified values from the input list
        for (int i = 0; i < labels.size(); i++) {
            jreJsonArray.set(i, labels.get(i).toString());
        }
        jreJsonObject.set("data", jreJsonArray);
        return jreJsonObject;
    }
}