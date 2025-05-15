package com.bervan.shstat.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.search.SearchRequest;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ScrapAuditService;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.vaadin.flow.data.provider.SortDirection;

import java.util.List;

public abstract class AbstractScrapAuditView extends AbstractTableView<Long, ScrapAudit> {
    public static final String ROUTE_NAME = "/shopping/scrap-audit";
    private final BervanLogger log;

    public AbstractScrapAuditView(ScrapAuditService scrapAuditService, BervanLogger log) {
        super(new ShoppingLayout(ROUTE_NAME), scrapAuditService, log, ScrapAudit.class);
        this.log = log;

        renderCommonComponents();

        addButton.setVisible(false);
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        sortField = "date";
        sortDirection = SortDirection.ASCENDING;
        request.setAddOwnerCriterion(false);
    }

    @Override
    protected List<String> getFieldsToFetchForTable() {
        List<String> fieldsToFetchForTable = super.getFieldsToFetchForTable();
        fieldsToFetchForTable.remove("productDetails");
        return fieldsToFetchForTable;
    }

    @Override
    protected List<ScrapAudit> loadData() {
        List<ScrapAudit> scrapAudits = super.loadData();
        scrapAudits.forEach(e -> e.setProductDetails(e.getProductDetails()));
        return scrapAudits;
    }

}
