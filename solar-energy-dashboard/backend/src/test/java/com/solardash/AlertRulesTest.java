package com.solardash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solardash.model.AlertType;
import com.solardash.model.Severity;
import com.solardash.rules.AlertRules;
import com.solardash.rules.AlertRules.Finding;
import com.solardash.rules.Sample;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertRulesTest {
    private static final double CAPACITY = 10;

    /** 24 hourly samples: night (expected 0) for hours 0-5 and 19-23, expected 8 kW from 6 to 18. */
    private static List<Sample> day(double factor, int zeroDaylightTail) {
        List<Sample> out = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            double expected = (hour >= 6 && hour <= 18) ? 8 : 0;
            out.add(new Sample(expected * factor, expected));
        }
        int zeroed = 0;
        for (int i = out.size() - 1; i >= 0 && zeroed < zeroDaylightTail; i--) {
            if (out.get(i).expectedKw() > 0) {
                out.set(i, new Sample(0, out.get(i).expectedKw()));
                zeroed++;
            }
        }
        return out;
    }

    @Test
    void healthySystemRaisesNothing() {
        assertTrue(AlertRules.evaluate(day(0.95, 0), CAPACITY).isEmpty());
    }

    @Test
    void moderateShortfallIsAWarning() {
        List<Finding> f = AlertRules.evaluate(day(0.70, 0), CAPACITY);
        assertEquals(1, f.size());
        assertEquals(AlertType.UNDERPERFORMANCE, f.get(0).type());
        assertEquals(Severity.WARNING, f.get(0).severity());
    }

    @Test
    void severeShortfallIsCritical() {
        List<Finding> f = AlertRules.evaluate(day(0.40, 0), CAPACITY);
        assertEquals(Severity.CRITICAL, f.get(0).severity());
    }

    @Test
    void zeroOutputDuringDaylightIsAnOutage() {
        List<Finding> f = AlertRules.evaluate(day(1.0, 4), CAPACITY);
        assertEquals(AlertType.OUTAGE, f.get(0).type());
        assertEquals(Severity.CRITICAL, f.get(0).severity());
    }

    @Test
    void shortDipIsNotAnOutage() {
        assertTrue(AlertRules.evaluate(day(1.0, 2), CAPACITY).stream()
                .noneMatch(x -> x.type() == AlertType.OUTAGE));
    }

    @Test
    void nightHoursAreIgnored() {
        List<Sample> night = new ArrayList<>();
        for (int i = 0; i < 24; i++) night.add(new Sample(0, 0));
        assertTrue(AlertRules.evaluate(night, CAPACITY).isEmpty());
    }
}
