package com.bervan.shstat.view;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.logging.JsonLogger;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.service.ProductBasedOnDateAttributesService;
import com.bervan.shstat.service.ProductService;
import com.vaadin.flow.component.grid.Grid;

public class PricesListView extends AbstractBervanTableView<Long, ProductBasedOnDateAttributes> {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");
    private final Product product;
    private final ProductService productService;
    private final AbstractProductView parentView;


    public PricesListView(AbstractProductView parentView, ProductBasedOnDateAttributesService service, ProductService productService, ShoppingLayout pageLayout, Product product, BervanViewConfig bervanViewConfig) {
        super(pageLayout, service, bervanViewConfig, ProductBasedOnDateAttributes.class);
        this.product = product;
        this.productService = productService;
        this.parentView = parentView;
        renderCommonComponents();
    }

    @Override
    protected void buildToolbarActionBar() {
        PricesListToolbar toolbar = new PricesListToolbar(checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, (v) -> {
            updateStatsOfProduct();
            return v;
        }, bervanViewConfig, (v) -> {
            refreshData();
            return v;
        }, service);

        // Pass floating toolbar for custom actions (if enabled)
        if (floatingToolbar != null) {
            toolbar.withFloatingToolbar(floatingToolbar);
        }

        tableToolbarActions = toolbar
                .withDecreasePrice2times()
                .withDecreasePrice5times()
                .withDecreasePrice10times()
                .withExportButton(isExportable(), service, () -> pathToFileStorage, () -> globalTmpDir)
                .build();
    }

    @Override
    protected Grid<ProductBasedOnDateAttributes> getGrid() {
        Grid<ProductBasedOnDateAttributes> grid = new Grid<>(ProductBasedOnDateAttributes.class, false);
        buildGridAutomatically(grid);

        return grid;
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.addCriterion("PRODUCT_TASK_CRITERIA", ProductBasedOnDateAttributes.class,
                "product.id", SearchOperation.EQUALS_OPERATION, product.getId());
        request.setAddOwnerCriterion(false);
        super.customizePreLoad(request);
    }

    @Override
    protected ProductBasedOnDateAttributes preSaveActions(ProductBasedOnDateAttributes newItem) {
        newItem.setProduct(product);
        return super.preSaveActions(newItem);
    }

    @Override
    protected void postSaveActions(ProductBasedOnDateAttributes save) {
        super.postSaveActions(save);
    }

    @Override
    protected void customPostUpdate(ProductBasedOnDateAttributes changed) {
        updateStatsOfProduct();
    }

    protected void updateStatsOfProduct() {
        productService.updateStats(product);
        parentView.reload();
    }
}