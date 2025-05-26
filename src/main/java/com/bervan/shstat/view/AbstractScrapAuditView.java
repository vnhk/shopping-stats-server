package com.bervan.shstat.view;

import com.bervan.common.AbstractTableView;
import com.bervan.common.BervanButton;
import com.bervan.common.BervanButtonStyle;
import com.bervan.common.search.SearchRequest;
import com.bervan.core.model.BervanLogger;
import com.bervan.shstat.ScrapAuditService;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.bervan.shstat.repository.ScrapAuditRepository;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScrapAuditView extends AbstractTableView<Long, ScrapAudit> {
    public static final String ROUTE_NAME = "/shopping/scrap-audit";
    private final BervanLogger log;
    private final ScrapAuditRepository scrapAuditRepository;
    private final HorizontalLayout buttonsWithDateFilters = new HorizontalLayout();
    private final BervanButton defaultThisDayButton = new BervanButton("Today", click -> {
        try {
            filtersLayout.getDateTimeFiltersMap().get(ScrapAudit.class.getDeclaredField("date"))
                    .get("FROM").setValue(LocalDate.now());

            filtersLayout.getDateTimeFiltersMap().get(ScrapAudit.class.getDeclaredField("date"))
                    .get("TO").setValue(LocalDate.now());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        refreshTable.click();
    }, BervanButtonStyle.PRIMARY);

    private final BervanButton yesterdayButton = new BervanButton("Yesterday", click -> {
        try {
            filtersLayout.getDateTimeFiltersMap().get(ScrapAudit.class.getDeclaredField("date"))
                    .get("FROM").setValue(LocalDate.now().minusDays(1));

            filtersLayout.getDateTimeFiltersMap().get(ScrapAudit.class.getDeclaredField("date"))
                    .get("TO").setValue(LocalDate.now().minusDays(1));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        refreshTable.click();
    }, BervanButtonStyle.PRIMARY);

    public AbstractScrapAuditView(ScrapAuditService scrapAuditService, ScrapAuditRepository scrapAuditRepository, BervanLogger log) {
        super(new ShoppingLayout(ROUTE_NAME), scrapAuditService, log, ScrapAudit.class);
        this.log = log;
        this.scrapAuditRepository = scrapAuditRepository;
        renderCommonComponents();

        buttonsWithDateFilters.add(defaultThisDayButton, yesterdayButton);

        topLayout.add(buttonsWithDateFilters);

        defaultThisDayButton.click();

        addButton.setVisible(false);
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.setAddOwnerCriterion(false);
    }

    @Override
    protected List<String> getFieldsToFetchForTable() {
        List<String> fieldsToFetchForTable = new ArrayList<>();
        fieldsToFetchForTable.add("id");
        return fieldsToFetchForTable;
    }

    @Override
    protected List<ScrapAudit> loadData() {
        List<ScrapAudit> scrapAudits = super.loadData();
        List<ScrapAudit> result = new ArrayList<>();

        scrapAudits.forEach(e -> {
            ScrapAudit scrapAudit = scrapAuditRepository.findById(e.getId()).get();
            scrapAudit.setProductDetails(scrapAudit.getProductDetails());
            result.add(scrapAudit);
        });
        return result;
    }

}
