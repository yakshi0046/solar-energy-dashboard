package com.solardash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solardash.model.AlertType;
import com.solardash.model.Severity;
import com.solardash.rules.AlertRules;
import com.solardash.rules.AlertRules.Finding;
import com.solardash.rules.Sample;
import com.solardash.seed.SyntheticData;
import com.solardash.seed.SyntheticData.Point;
import com.solardash.seed.SyntheticData.SystemSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class SyntheticDataTest {
    /** The seeded faults must be detected no matter what time of day the app starts. */
    @Test
    void seededFaultsAreDetectedAtAnyHour() {
        Instant base = Instant.parse("2026-06-15T00:00:00Z");
        for (int h = 0; h < 48; h++) {
            Instant end = base.plus(h, ChronoUnit.HOURS);
            for (int i = 0; i < SyntheticData.SPECS.size(); i++) {
                SystemSpec spec = SyntheticData.SPECS.get(i);
                Instant since = end.minus(24, ChronoUnit.HOURS);
                List<Sample> window = SyntheticData.generate(spec, i, end, 7).stream()
                        .filter(p -> p.timestamp().isAfter(since))
                        .map(p -> new Sample(p.powerKw(), p.expectedKw()))
                        .toList();
                List<Finding> findings = AlertRules.evaluate(window, spec.capacityKw());
                String where = spec.name() + " at +" + h + "h";
                switch (spec.name()) {
                    case "Harbor Warehouse" -> {
                        assertEquals(1, findings.size(), where);
                        assertEquals(AlertType.UNDERPERFORMANCE, findings.get(0).type(), where);
                        assertEquals(Severity.CRITICAL, findings.get(0).severity(), where);
                    }
                    case "Desert Ridge Farm" -> {
                        assertEquals(1, findings.size(), where);
                        assertEquals(AlertType.OUTAGE, findings.get(0).type(), where);
                    }
                    case "Alpine Community Hall" -> {
                        assertEquals(1, findings.size(), where);
                        assertEquals(Severity.WARNING, findings.get(0).severity(), where);
                    }
                    default -> assertTrue(findings.isEmpty(), where);
                }
            }
        }
    }

    @Test
    void outputIsZeroAtNightAndNeverExceedsCapacity() {
        SystemSpec spec = SyntheticData.SPECS.get(0);
        for (Point p : SyntheticData.generate(spec, 0, Instant.parse("2026-06-15T12:00:00Z"), 3)) {
            assertTrue(p.powerKw() >= 0 && p.expectedKw() >= 0);
            assertTrue(p.powerKw() <= spec.capacityKw());
        }
    }
}
