package com.bervan.shstat.service;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.common.service.EmailService;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.repository.ProductAlertRepository;
import com.bervan.shstat.response.PriceDTO;
import com.bervan.shstat.response.ProductDTO;
import com.bervan.shstat.response.SearchApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    private static boolean isMatch(ProductAlert alert, ProductDTO product) {
        BigDecimal price = null;
        if (product.getPrices() != null && !product.getPrices().isEmpty()) {
            price = product.getPrices().get(0).getPrice();
        }

        Double discount = product.getDiscount();

        boolean match = alert.getProductName() == null || product.getName().toLowerCase().contains(alert.getProductName().toLowerCase());

        if (alert.getPriceMin() != null && (price == null || price.doubleValue() < alert.getPriceMin())) {
            match = false;
        }
        if (alert.getPriceMax() != null && (price == null || price.doubleValue() > alert.getPriceMax())) {
            match = false;
        }

        if (alert.getDiscountMin() != null && discount < alert.getDiscountMin()) {
            match = false;
        }
        if (alert.getDiscountMax() != null && discount > alert.getDiscountMax()) {
            match = false;
        }

        if (alert.getProductCategories() != null && !alert.getProductCategories().isEmpty()) {
            if (Collections.disjoint(alert.getProductCategories(), product.getCategories())) {
                match = false;
            }
        }
        return match;
    }

    private static String getDiscountStr(ProductDTO product, PriceDTO latest) {
        BigDecimal latestPrice = latest.getPrice();
        BigDecimal avgPrice = product.getAvgPrice();

        String discount = null;
        if (avgPrice != null && latestPrice != null && avgPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = latestPrice.subtract(avgPrice);
            BigDecimal percentage = diff.abs().divide(avgPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            if (latestPrice.compareTo(avgPrice) > 0) {
                discount = "+" + percentage;
            } else if (latestPrice.compareTo(avgPrice) < 0) {
                discount = "-" + percentage;
            }
        }
        return discount;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void notifyAboutProducts() {
        log.info("notifyAboutProducts[scheduled] started");

        SearchRequest request = new SearchRequest();
        request.setAddOwnerCriterion(false);
        Set<ProductAlert> productAlerts = load(request, Pageable.ofSize(100000));

        notifyAboutProducts(productAlerts);
        log.info("notifyAboutProducts[scheduled] ended");
    }

    public void notifyAboutProducts(Collection<ProductAlert> alerts) {
        log.info("notifyAboutProducts started");
        for (ProductAlert alert : alerts) {
            try {
                notifyAboutProducts(alert);
            } catch (Exception e) {
                log.error("Could not notify about product prices: alert name = {}", alert.getName(), e);
            }
        }
        log.info("notifyAboutProducts ended");
    }

    private void notifyAboutProducts(ProductAlert alert) {
        if (alert.getEmails() == null || alert.getEmails().isEmpty()) {
            log.warn("Alert without emails. Skipping.");
            return;
        }

        Set<String> categories = new HashSet<>();
        int minDis = 0;
        int maxDis = 100;

        if (alert.getDiscountMin() != null) {
            minDis = alert.getDiscountMin();
        }
        if (alert.getDiscountMax() != null) {
            maxDis = alert.getDiscountMax();
        }
        if (alert.getProductCategories() != null && !alert.getProductCategories().isEmpty()) {
            categories.addAll(alert.getProductCategories());
        }

        SearchApiResponse discountsCompared = discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(Pageable.ofSize(100000), (double) minDis, (double) maxDis, 3, new ArrayList<>(categories), null, null, 10, 1_000_000);

        List<ProductDTO> discountedProducts = (List<ProductDTO>) discountsCompared.getItems().stream().sorted((p1, p2) -> {
            Double discount1 = ((ProductDTO) p1).getDiscount();
            Double discount2 = ((ProductDTO) p2).getDiscount();
            return discount2.compareTo(discount1);
        }).collect(Collectors.toList());

        List<ProductDTO> matchedProducts = new ArrayList<>();

        for (ProductDTO product : discountedProducts) {
            boolean match = isMatch(alert, product);

            if (match) {
                matchedProducts.add(product);
            }
        }

        log.info("notifyAboutProducts - found {} matched products for alert {} id", matchedProducts.size(), alert.getId());

        if (!matchedProducts.isEmpty()) {
            String message = buildHtmlProductList(alert, matchedProducts.subList(0, Math.min(100, matchedProducts.size())));
            for (String email : alert.getEmails()) {
                emailService.sendEmail(email, "Product Alerts " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-uuuu")), message);
            }
        }
    }

    private String buildHtmlProductList(ProductAlert alert, List<ProductDTO> products) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <html>
                <body>
                """);
        html.append("<h2>🔥 New Product Alerts - ").append(alert.getName()).append("</h2> <ul>");

        for (ProductDTO product : products) {
            String name = product.getName();
            String link = product.getOfferLink();

            List<PriceDTO> prices = product.getPrices();
            PriceDTO latest = prices.get(0);
            PriceDTO previous = prices.size() > 1 ? prices.get(1) : null;
            PriceDTO minPrice = product.getMinPrice();

            String discount = getDiscountStr(product, latest);

            html.append(String.format("""
                        <li>
                            <a href="%s">%s</a><br/>
                            <strong>Current price:</strong> %.2f PLN<br/>
                            %s
                            %s
                            <strong>Min price:</strong> %.2f PLN
                        </li>
                    """, link, name, latest.getPrice() != null ? latest.getPrice() : BigDecimal.ZERO, previous != null && previous.getPrice() != null ? String.format("<strong>Previous price:</strong> %.2f PLN<br/>", previous.getPrice()) : "", discount != null ? String.format("<strong>Discount:</strong> %s%%<br/>", discount) : "", minPrice != null && minPrice.getPrice() != null ? minPrice.getPrice() : BigDecimal.ZERO));
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
