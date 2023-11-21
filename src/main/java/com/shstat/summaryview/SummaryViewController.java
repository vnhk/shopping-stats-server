package com.shstat.summaryview;

import com.shstat.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/summary-view/products")
public class SummaryViewController {
    private final SummaryViewService summaryViewService;

    public SummaryViewController(SummaryViewService summaryViewService) {
        this.summaryViewService = summaryViewService;
    }

    @GetMapping(path = "/product")
    public ResponseEntity<ApiResponse> findProductContainingName(@RequestParam String name) {
        return ResponseEntity.ok(summaryViewService.findProductContainingName(name));
    }
}
