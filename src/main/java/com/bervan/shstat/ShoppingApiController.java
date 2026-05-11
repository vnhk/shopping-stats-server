package com.bervan.shstat;

import com.bervan.common.search.SearchRequest;
import com.bervan.shstat.entity.ProductAlert;
import com.bervan.shstat.entity.scrap.ProductConfig;
import com.bervan.shstat.entity.scrap.ScrapAudit;
import com.bervan.shstat.entity.scrap.ShopConfig;
import com.bervan.shstat.response.SearchApiResponse;
import com.bervan.shstat.service.*;
import com.bervan.shstat.view.ProductViewService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shopping")
@RolesAllowed({"USER"})
public class ShoppingApiController {

    private final ProductViewService productViewService;
    private final DiscountsViewService discountsViewService;
    private final ProductSearchService productSearchService;
    private final ProductAlertService productAlertService;
    private final ShopConfigService shopConfigService;
    private final ProductConfigService productConfigService;
    private final ScrapAuditService scrapAuditService;

    public ShoppingApiController(ProductViewService productViewService,
                                 DiscountsViewService discountsViewService,
                                 ProductSearchService productSearchService,
                                 ProductAlertService productAlertService,
                                 ShopConfigService shopConfigService,
                                 ProductConfigService productConfigService,
                                 ScrapAuditService scrapAuditService) {
        this.productViewService = productViewService;
        this.discountsViewService = discountsViewService;
        this.productSearchService = productSearchService;
        this.productAlertService = productAlertService;
        this.shopConfigService = shopConfigService;
        this.productConfigService = productConfigService;
        this.scrapAuditService = scrapAuditService;
    }

    // ── Products ─────────────────────────────────────────────────────────────

    @GetMapping("/products")
    public ResponseEntity<SearchApiResponse> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String shop,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchApiResponse response = productViewService.findProducts(category, shop, name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<SearchApiResponse> getProduct(@PathVariable Long id) {
        SearchApiResponse response = productViewService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/categories")
    public ResponseEntity<Set<String>> getCategories() {
        return ResponseEntity.ok(productSearchService.findCategories());
    }

    // ── Best Offers ───────────────────────────────────────────────────────────

    @GetMapping("/best-offers")
    public ResponseEntity<SearchApiResponse> getBestOffers(
            @RequestParam(required = false) Double discountMin,
            @RequestParam(required = false) Double discountMax,
            @RequestParam(defaultValue = "3") Integer months,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String shop,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer prevPriceMin,
            @RequestParam(required = false) Integer prevPriceMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<String> cats = categories != null ? categories : new ArrayList<>();
        SearchApiResponse response = discountsViewService.findDiscountsComparedToAVGOnPricesInLastXMonths(
                pageable,
                discountMin != null ? discountMin : 0.0,
                discountMax != null ? discountMax : 100.0,
                months, cats, shop, name, prevPriceMin, prevPriceMax);
        return ResponseEntity.ok(response);
    }

    // ── Product Alerts ────────────────────────────────────────────────────────

    @GetMapping("/product-alerts")
    public ResponseEntity<List<ProductAlertDto>> getProductAlerts() {
        SearchRequest request = new SearchRequest();
        Set<ProductAlert> alerts = productAlertService.load(request, Pageable.ofSize(10000));
        List<ProductAlertDto> dtos = alerts.stream()
                .map(a -> new ProductAlertDto(a.getId(), a.getName(), a.getPriceMin(), a.getPriceMax(),
                        a.getDiscountMin(), a.getDiscountMax(), a.getProductName(),
                        a.getProductCategories(), a.getEmails()))
                .sorted(Comparator.comparing(d -> d.name() != null ? d.name() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/product-alerts")
    public ResponseEntity<ProductAlertDto> createProductAlert(@RequestBody ProductAlertDto dto) {
        ProductAlert alert = new ProductAlert();
        applyAlertDto(alert, dto);
        ProductAlert saved = productAlertService.save(alert);
        return ResponseEntity.ok(toAlertDto(saved));
    }

    @PutMapping("/product-alerts/{id}")
    public ResponseEntity<ProductAlertDto> updateProductAlert(@PathVariable Long id, @RequestBody ProductAlertDto dto) {
        ProductAlert alert = productAlertService.findById(id);
        if (alert == null) return ResponseEntity.notFound().build();
        applyAlertDto(alert, dto);
        ProductAlert saved = productAlertService.save(alert);
        return ResponseEntity.ok(toAlertDto(saved));
    }

    @DeleteMapping("/product-alerts/{id}")
    public ResponseEntity<Void> deleteProductAlert(@PathVariable Long id) {
        productAlertService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyAlertDto(ProductAlert alert, ProductAlertDto dto) {
        alert.setName(dto.name());
        alert.setPriceMin(dto.priceMin());
        alert.setPriceMax(dto.priceMax());
        alert.setDiscountMin(dto.discountMin());
        alert.setDiscountMax(dto.discountMax());
        alert.setProductName(dto.productName());
        alert.setProductCategories(dto.productCategories() != null ? dto.productCategories() : new ArrayList<>());
        alert.setEmails(dto.emails() != null ? dto.emails() : new ArrayList<>());
    }

    private ProductAlertDto toAlertDto(ProductAlert a) {
        return new ProductAlertDto(a.getId(), a.getName(), a.getPriceMin(), a.getPriceMax(),
                a.getDiscountMin(), a.getDiscountMax(), a.getProductName(),
                a.getProductCategories(), a.getEmails());
    }

    @GetMapping("/shop-configs")
    public ResponseEntity<List<ShopConfigDto>> getShopConfigs() {
        SearchRequest request = new SearchRequest();
        request.setAddOwnerCriterion(false);
        Set<ShopConfig> shops = shopConfigService.load(request, Pageable.ofSize(10000));
        List<ShopConfigDto> dtos = shops.stream()
                .map(s -> new ShopConfigDto(s.getId(), s.getShopName(), s.getBaseUrl()))
                .sorted(Comparator.comparing(d -> d.shopName() != null ? d.shopName() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ── Shop Configs ──────────────────────────────────────────────────────────

    @PostMapping("/shop-configs")
    public ResponseEntity<ShopConfigDto> createShopConfig(@RequestBody ShopConfigDto dto) {
        ShopConfig shop = new ShopConfig();
        applyShopDto(shop, dto);
        ShopConfig saved = shopConfigService.save(shop);
        return ResponseEntity.ok(new ShopConfigDto(saved.getId(), saved.getShopName(), saved.getBaseUrl()));
    }

    @PutMapping("/shop-configs/{id}")
    public ResponseEntity<ShopConfigDto> updateShopConfig(@PathVariable Long id, @RequestBody ShopConfigDto dto) {
        ShopConfig shop = shopConfigService.findById(id);
        if (shop == null) return ResponseEntity.notFound().build();
        applyShopDto(shop, dto);
        ShopConfig saved = shopConfigService.save(shop);
        return ResponseEntity.ok(new ShopConfigDto(saved.getId(), saved.getShopName(), saved.getBaseUrl()));
    }

    @DeleteMapping("/shop-configs/{id}")
    public ResponseEntity<Void> deleteShopConfig(@PathVariable Long id) {
        shopConfigService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyShopDto(ShopConfig shop, ShopConfigDto dto) {
        shop.setShopName(dto.shopName());
        shop.setBaseUrl(dto.baseUrl());
    }

    @GetMapping("/product-configs")
    public ResponseEntity<List<ProductConfigDto>> getProductConfigs(@RequestParam(required = false) Long shopId) {
        SearchRequest request = new SearchRequest();
        request.setAddOwnerCriterion(false);
        Set<ProductConfig> configs = productConfigService.load(request, Pageable.ofSize(10000));
        List<ProductConfigDto> dtos = configs.stream()
                .filter(c -> shopId == null || (c.getShop() != null && c.getShop().getId().equals(shopId)))
                .map(c -> new ProductConfigDto(
                        c.getId(), c.getName(), c.getUrl(), c.getMinPrice(), c.getMaxPrice(),
                        c.getScrapTime() != null ? c.getScrapTime().toString() : null,
                        c.getCategories(),
                        c.getShop() != null ? c.getShop().getId() : null,
                        c.getShop() != null ? c.getShop().getShopName() : null))
                .sorted(Comparator.comparing(d -> d.name() != null ? d.name() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/product-configs")
    public ResponseEntity<ProductConfigDto> createProductConfig(@RequestBody ProductConfigDto dto) {
        ProductConfig config = new ProductConfig();
        applyProductConfigDto(config, dto);
        ProductConfig saved = productConfigService.save(config);
        return ResponseEntity.ok(toProductConfigDto(saved));
    }

    // ── Product Configs ───────────────────────────────────────────────────────

    @PutMapping("/product-configs/{id}")
    public ResponseEntity<ProductConfigDto> updateProductConfig(@PathVariable Long id, @RequestBody ProductConfigDto dto) {
        ProductConfig config = productConfigService.findById(id);
        if (config == null) return ResponseEntity.notFound().build();
        applyProductConfigDto(config, dto);
        ProductConfig saved = productConfigService.save(config);
        return ResponseEntity.ok(toProductConfigDto(saved));
    }

    @DeleteMapping("/product-configs/{id}")
    public ResponseEntity<Void> deleteProductConfig(@PathVariable Long id) {
        productConfigService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyProductConfigDto(ProductConfig config, ProductConfigDto dto) {
        config.setName(dto.name());
        config.setUrl(dto.url());
        config.setMinPrice(dto.minPrice());
        config.setMaxPrice(dto.maxPrice());
        config.setScrapTime(dto.scrapTime() != null ? LocalTime.parse(dto.scrapTime()) : null);
        config.setCategories(dto.categories() != null ? dto.categories() : new ArrayList<>());
        if (dto.shopId() != null) {
            ShopConfig shop = shopConfigService.findById(dto.shopId());
            config.setShop(shop);
        }
    }

    private ProductConfigDto toProductConfigDto(ProductConfig c) {
        return new ProductConfigDto(
                c.getId(), c.getName(), c.getUrl(), c.getMinPrice(), c.getMaxPrice(),
                c.getScrapTime() != null ? c.getScrapTime().toString() : null,
                c.getCategories(),
                c.getShop() != null ? c.getShop().getId() : null,
                c.getShop() != null ? c.getShop().getShopName() : null);
    }

    @GetMapping("/scrap-audits")
    public ResponseEntity<List<ScrapAuditDto>> getScrapAudits(@RequestParam(required = false) String date) {
        SearchRequest request = new SearchRequest();
        request.setAddOwnerCriterion(false);
        Set<ScrapAudit> audits = scrapAuditService.load(request, Pageable.ofSize(10000));
        List<ScrapAuditDto> dtos = audits.stream()
                .filter(a -> {
                    if (date == null) return true;
                    return a.getDate() != null && a.getDate().toString().equals(date);
                })
                .map(a -> new ScrapAuditDto(
                        a.getId(),
                        a.getDate() != null ? a.getDate().toString() : null,
                        a.getSavedProducts(),
                        a.getProductDetails()))
                .sorted(Comparator.comparing((ScrapAuditDto d) -> d.date() != null ? d.date() : "").reversed())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/scrap-audits/{id}")
    public ResponseEntity<Void> deleteScrapAudit(@PathVariable Long id) {
        scrapAuditService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    record ProductAlertDto(Long id, String name, Integer priceMin, Integer priceMax,
                           Integer discountMin, Integer discountMax, String productName,
                           List<String> productCategories, List<String> emails) {
    }

    // ── Scrap Audits ──────────────────────────────────────────────────────────

    record ShopConfigDto(Long id, String shopName, String baseUrl) {
    }

    record ProductConfigDto(Long id, String name, String url, Integer minPrice, Integer maxPrice,
                            String scrapTime, List<String> categories, Long shopId, String shopName) {
    }

    record ScrapAuditDto(Long id, String date, long savedProducts, String productDetails) {
    }
}
