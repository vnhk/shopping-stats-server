package com.bervan.shstat.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.BervanLoggerImpl;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.service.BaseService;
import com.bervan.common.user.UserRepository;
import com.bervan.shstat.ProductService;
import com.bervan.shstat.entity.Product;
import com.bervan.shstat.entity.ProductBasedOnDateAttributes;
import com.vaadin.flow.component.grid.Grid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PricesListView extends AbstractTableView<Long, ProductBasedOnDateAttributes> {
    private final Product product;
    private final ProductService productService;
    private final UserRepository userRepository;

    public PricesListView(BaseService<Long, ProductBasedOnDateAttributes> service, ProductService productService, ShoppingLayout pageLayout, Product product, UserRepository userRepository) {
        super(pageLayout, service, BervanLoggerImpl.init(log), ProductBasedOnDateAttributes.class);
        this.product = product;
        this.productService = productService;
        this.userRepository = userRepository;
        renderCommonComponents();
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
    protected ProductBasedOnDateAttributes customizeSavingInCreateForm(ProductBasedOnDateAttributes newItem) {
        newItem.setProduct(product);
        newItem.addOwner(userRepository.findByUsername("COMMON_USER").get());
        return super.customizeSavingInCreateForm(newItem);
    }

    @Override
    protected void postSaveActions() {
        super.postSaveActions();
    }

    @Override
    protected void customPostUpdate(ProductBasedOnDateAttributes changed) {
        productService.updateStats(product);
    }
}