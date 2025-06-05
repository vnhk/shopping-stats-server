package com.bervan.shstat.service;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.EmailService;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.repository.ProductAlertRepository;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class ProductAlertService extends BaseService<Long, ProductAlert> {
    private final EmailService emailService;
    private final DiscountsViewService discountsViewService;

    protected ProductAlertService(EmailService emailService, ProductAlertRepository repository, DiscountsViewService discountsViewService, SearchService searchService) {
        super(repository, searchService);
        this.emailService = emailService;
        this.discountsViewService = discountsViewService;
    }

    @Scheduled(cron = "0 0 0/12 * * *")
    public void notifyAboutProducts() {
        log.info("notifyAboutProducts started");

        Integer minDis = Integer.MAX_VALUE;
        Integer maxDis = Integer.MIN_VALUE;
        Set<String> categories = new HashSet<>();
        SearchRequest request = new SearchRequest();
        request.setAddOwnerCriterion(false);
        Set<ProductAlert> productAlerts = load(request, Pageable.ofSize(100000));

        Map<String, List<ProductAlert>> alertsByEmail = new HashMap<>();

        for (ProductAlert alert : productAlerts) {
            if (alert.getDiscountMin() != null) {
                minDis = Math.min(minDis, alert.getDiscountMin());
            }
            if (alert.getDiscountMax() != null) {
                maxDis = Math.max(maxDis, alert.getDiscountMax());
            }
            if (alert.getProductCategories() != null) {
                categories.addAll(alert.getProductCategories());
            }
            if (alert.getEmails() != null) {
                for (String email : alert.getEmails()) {
                    alertsByEmail.computeIfAbsent(email, e -> new ArrayList<>()).add(alert);
                }
            }
        }

        if (productAlerts.isEmpty()) {
            return;
        }

        SearchApiResponse discountsCompared = discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(
                Pageable.ofSize(1000),
                minDis.doubleValue(),
                maxDis.doubleValue(),
                3,
                new ArrayList<>(categories),
                null, null,
                50, 1_000_000
        );

        List<ProductDTO> discountedProducts = discountsCompared.getItems().stream()
                .map(item -> (ProductDTO) item)
                .toList();

        for (Map.Entry<String, List<ProductAlert>> entry : alertsByEmail.entrySet()) {
            String email = entry.getKey();
            List<ProductAlert> alerts = entry.getValue();
            List<ProductDTO> matchedProducts = new ArrayList<>();

            for (ProductDTO product : discountedProducts) {
                BigDecimal price = null;
                if (product.getPrices() != null && !product.getPrices().isEmpty()) {
                    price = product.getPrices().get(0).getPrice();
                }

                for (ProductAlert alert : alerts) {
                    boolean match = true;

                    if (alert.getProductName() != null && !product.getName().toLowerCase().contains(alert.getProductName().toLowerCase())) {
                        match = false;
                    }

                    if (alert.getPriceMin() != null && (price == null || price.doubleValue() < alert.getPriceMin())) {
                        match = false;
                    }
                    if (alert.getPriceMax() != null && (price == null || price.doubleValue() > alert.getPriceMax())) {
                        match = false;
                    }

                    if (alert.getDiscountMin() != null && product.getDiscount() != null && product.getDiscount() < alert.getDiscountMin()) {
                        match = false;
                    }
                    if (alert.getDiscountMax() != null && product.getDiscount() != null && product.getDiscount() > alert.getDiscountMax()) {
                        match = false;
                    }

                    if (alert.getProductCategories() != null && !alert.getProductCategories().isEmpty()) {
                        if (Collections.disjoint(alert.getProductCategories(), product.getCategories())) {
                            match = false;
                        }
                    }

                    if (match) {
                        matchedProducts.add(product);
                        break;
                    }
                }
            }

            if (!matchedProducts.isEmpty()) {
                String message = buildHtmlProductList(matchedProducts.subList(0, Math.min(50, matchedProducts.size())));
                emailService.sendEmail(email, "Product Alerts", message);
            }
        }
        log.info("notifyAboutProducts ended");
    }

    public String buildHtmlProductList(List<ProductDTO> products) {
        StringBuilder html = new StringBuilder();
        html.append("""
                    <html>
                    <body>
                        <h2>🔥 New Product Alerts</h2>
                        <ul>
                """);

        for (ProductDTO product : products) {
            String name = product.getName();
            String link = product.getOfferLink();
            BigDecimal price = product.getPrices().get(0).getPrice();
            Double discount = product.getDiscount();

            html.append(String.format("""
                                <li>
                                    <a href="%s">%s</a> – <strong>%.2f PLN</strong>
                                    %s
                                </li>
                            """, link, name, price != null ? price : BigDecimal.ZERO,
                    discount != null ? String.format("(-%.0f%%)", discount) : ""));
        }

        html.append("""
                        </ul>
                        <p style="font-size: 12px; color: #888;">This is an automatic message – please do not reply.</p>
                    </body>
                    </html>
                """);

        return html.toString();
    }

    public List<String> loadAllCategories(ProductAlert productAlert) {
        return ((ProductAlertRepository) repository).loadAllCategories(productAlert);
    }

    public List<String> loadAllEmails(ProductAlert productAlert) {
        return ((ProductAlertRepository) repository).loadAllEmails(productAlert);
    }
}
