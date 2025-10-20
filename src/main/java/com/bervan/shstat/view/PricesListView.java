package com.bervan.shstat.view;

import com.bervan.common.BervanLoggerImpl;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.user.UserRepository;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.bervan.shstat.service.ProductBasedOnDateAttributesService;
import com.bervan.shstat.service.ProductService;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PricesListView extends AbstractBervanTableView<Long, ProductBasedOnDateAttributes> {
    private final Product product;
    private final ProductService productService;
    private final ProductViewService productViewService;
    private final UserRepository userRepository;
    private final AbstractProductView parentView;


    public PricesListView(AbstractProductView parentView, ProductBasedOnDateAttributesService service, ProductService productService, ShoppingLayout pageLayout, Product product, ProductViewService productViewService, UserRepository userRepository, BervanViewConfig bervanViewConfig) {
        super(pageLayout, service, BervanLoggerImpl.init(log), bervanViewConfig, ProductBasedOnDateAttributes.class);
        this.product = product;
        this.productService = productService;
        this.parentView = parentView;
        this.productViewService = productViewService;
        this.userRepository = userRepository;
        renderCommonComponents();
    }

    @Override
    protected void buildToolbarActionBar() {
        tableToolbarActions = new PricesListToolbar(gridActionService, checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, (V) -> {
            updateStatsOfProduct();
            return null;
        }, bervanViewConfig)
                .withDecreasePrice2times()
                .withDecreasePrice5times()
                .withDecreasePrice10times()
                .withExportButton(isExportable(), service, bervanLogger, pathToFileStorage, globalTmpDir)
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
        newItem.addOwner(userRepository.findByUsername("COMMON_USER").get());
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