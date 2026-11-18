package com.solardash.service;

import com.solardash.model.Alert;
import com.solardash.model.Reading;
import com.solardash.model.Severity;
import com.solardash.repo.AlertRepository;
import com.solardash.repo.ReadingRepository;
import com.solardash.repo.SolarSystemRepository;
import com.solardash.web.Dtos.AlertView;
import com.solardash.web.Dtos.FleetSummary;
import com.solardash.web.Dtos.ReadingPoint;
import com.solardash.web.Dtos.SystemSummary;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final SolarSystemRepository systems;
    private final ReadingRepository readings;
    private final AlertRepository alerts;

    public DashboardService(SolarSystemRepository systems, ReadingRepository readings, AlertRepository alerts) {
        this.systems = systems;
        this.readings = readings;
        this.alerts = alerts;
    }

    public List<SystemSummary> systems() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        Map<Long, Severity> worst = new HashMap<>();
        for (Alert a : alerts.findOpen()) {
            worst.merge(a.getSystem().getId(), a.getSeverity(), (x, y) -> x.ordinal() >= y.ordinal() ? x : y);
        }
        return systems.findAll(Sort.by("id")).stream().map(s -> {
            List<Reading> window = readings.findBySystemIdAndTimestampAfterOrderByTimestampAsc(s.getId(), since);
            double actual = window.stream().mapToDouble(Reading::getPowerKw).sum();
            double expected = window.stream().mapToDouble(Reading::getExpectedKw).sum();
            double current = window.isEmpty() ? 0 : window.get(window.size() - 1).getPowerKw();
            double ratio = expected > 0 ? actual / expected : 1.0;
            Severity severity = worst.get(s.getId());
            String status = severity == null ? "HEALTHY" : severity.name();
            return new SystemSummary(s.getId(), s.getName(), s.getLocation(), s.getZoneId(), s.getCapacityKw(),
                    current, round(actual), round(expected), round(ratio), status);
        }).toList();
    }

    public FleetSummary summary() {
        List<SystemSummary> all = systems();
        return new FleetSummary(
                all.size(),
                round(all.stream().mapToDouble(SystemSummary::capacityKw).sum()),
                round(all.stream().mapToDouble(SystemSummary::currentPowerKw).sum()),
                round(all.stream().mapToDouble(SystemSummary::energy24hKwh).sum()),
                alerts.findOpen().size());
    }

    public List<ReadingPoint> readings(Long systemId, int hours) {
        if (!systems.existsById(systemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown system " + systemId);
        }
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return readings.findBySystemIdAndTimestampAfterOrderByTimestampAsc(systemId, since).stream()
                .map(r -> new ReadingPoint(r.getTimestamp(), r.getPowerKw(), r.getExpectedKw()))
                .toList();
    }

    public List<AlertView> alerts(boolean openOnly) {
        List<Alert> found = openOnly ? alerts.findOpen() : alerts.findAllWithSystem();
        return found.stream()
                .map(a -> new AlertView(a.getId(), a.getSystem().getId(), a.getSystem().getName(),
                        a.getType(), a.getSeverity(), a.getMessage(), a.getCreatedAt()))
                .toList();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
