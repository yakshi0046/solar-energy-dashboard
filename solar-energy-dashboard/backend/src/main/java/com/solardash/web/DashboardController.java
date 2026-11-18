package com.solardash.web;

import com.solardash.service.AlertService;
import com.solardash.service.DashboardService;
import com.solardash.web.Dtos.AlertView;
import com.solardash.web.Dtos.FleetSummary;
import com.solardash.web.Dtos.ReadingPoint;
import com.solardash.web.Dtos.SystemSummary;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboard;
    private final AlertService alertService;

    public DashboardController(DashboardService dashboard, AlertService alertService) {
        this.dashboard = dashboard;
        this.alertService = alertService;
    }

    @GetMapping("/summary")
    public FleetSummary summary() {
        return dashboard.summary();
    }

    @GetMapping("/systems")
    public List<SystemSummary> systems() {
        return dashboard.systems();
    }

    @GetMapping("/systems/{id}/readings")
    public List<ReadingPoint> readings(@PathVariable Long id, @RequestParam(defaultValue = "48") int hours) {
        int clamped = Math.max(1, Math.min(hours, 168));
        return dashboard.readings(id, clamped);
    }

    /** status=open (default) returns unresolved alerts only; status=all includes resolved ones. */
    @GetMapping("/alerts")
    public List<AlertView> alerts(@RequestParam(defaultValue = "open") String status) {
        return dashboard.alerts(!"all".equalsIgnoreCase(status));
    }

    @PostMapping("/alerts/evaluate")
    public Map<String, Integer> evaluate() {
        return Map.of("changes", alertService.evaluateAll());
    }
}
