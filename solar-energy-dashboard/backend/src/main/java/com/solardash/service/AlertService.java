package com.solardash.service;

import com.solardash.model.Alert;
import com.solardash.model.SolarSystem;
import com.solardash.repo.AlertRepository;
import com.solardash.repo.ReadingRepository;
import com.solardash.repo.SolarSystemRepository;
import com.solardash.rules.AlertRules;
import com.solardash.rules.AlertRules.Finding;
import com.solardash.rules.Sample;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {
    private final SolarSystemRepository systems;
    private final ReadingRepository readings;
    private final AlertRepository alerts;

    public AlertService(SolarSystemRepository systems, ReadingRepository readings, AlertRepository alerts) {
        this.systems = systems;
        this.readings = readings;
        this.alerts = alerts;
    }

    /** Re-checks every system. Opens new alerts, updates changed ones and resolves cleared ones. Returns the number of changes. */
    @Transactional
    public int evaluateAll() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        int changes = 0;
        for (SolarSystem system : systems.findAll()) {
            List<Sample> window = readings
                    .findBySystemIdAndTimestampAfterOrderByTimestampAsc(system.getId(), since).stream()
                    .map(r -> new Sample(r.getPowerKw(), r.getExpectedKw()))
                    .toList();
            List<Finding> findings = AlertRules.evaluate(window, system.getCapacityKw());
            List<Alert> open = alerts.findBySystemIdAndResolvedFalse(system.getId());

            for (Alert alert : open) {
                Finding match = findings.stream()
                        .filter(f -> f.type() == alert.getType())
                        .findFirst()
                        .orElse(null);
                if (match == null) {
                    alert.setResolved(true);
                    changes++;
                } else {
                    alert.setSeverity(match.severity());
                    alert.setMessage(match.message());
                }
            }
            for (Finding finding : findings) {
                boolean alreadyOpen = open.stream().anyMatch(a -> a.getType() == finding.type());
                if (!alreadyOpen) {
                    alerts.save(new Alert(system, finding.type(), finding.severity(), finding.message()));
                    changes++;
                }
            }
        }
        return changes;
    }
}
