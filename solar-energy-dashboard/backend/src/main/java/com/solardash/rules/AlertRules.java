package com.solardash.rules;

import com.solardash.model.AlertType;
import com.solardash.model.Severity;
import java.util.List;

/**
 * Pure alert logic. Input is the last 24h of hourly samples, oldest first.
 * Night hours are ignored: a sample only counts if the system should be producing (expected output
 * above 5% of nameplate capacity).
 */
public final class AlertRules {
    public static final double WARNING_RATIO = 0.80;
    public static final double CRITICAL_RATIO = 0.50;
    public static final int OUTAGE_STREAK = 3;
    private static final double DAYLIGHT_FRACTION = 0.05;
    private static final double ZERO_FRACTION = 0.02;

    public record Finding(AlertType type, Severity severity, String message) {}

    private AlertRules() {}

    public static List<Finding> evaluate(List<Sample> window, double capacityKw) {
        List<Sample> daylight = window.stream()
                .filter(s -> s.expectedKw() > DAYLIGHT_FRACTION * capacityKw)
                .toList();
        if (daylight.isEmpty()) {
            return List.of();
        }

        // Outage: the most recent daylight hours all read (almost) zero.
        int streak = 0;
        for (int i = daylight.size() - 1; i >= 0; i--) {
            Sample s = daylight.get(i);
            if (s.powerKw() < ZERO_FRACTION * s.expectedKw()) {
                streak++;
            } else {
                break;
            }
        }
        if (streak >= OUTAGE_STREAK) {
            return List.of(new Finding(AlertType.OUTAGE, Severity.CRITICAL,
                    "No output for the last %d daylight hours although sunshine was expected.".formatted(streak)));
        }

        // Underperformance: total output vs weather-adjusted expectation over the window.
        double actual = daylight.stream().mapToDouble(Sample::powerKw).sum();
        double expected = daylight.stream().mapToDouble(Sample::expectedKw).sum();
        double ratio = actual / expected;
        if (ratio < CRITICAL_RATIO) {
            return List.of(new Finding(AlertType.UNDERPERFORMANCE, Severity.CRITICAL,
                    "Producing %.0f%% of expected output over the last 24 hours.".formatted(ratio * 100)));
        }
        if (ratio < WARNING_RATIO) {
            return List.of(new Finding(AlertType.UNDERPERFORMANCE, Severity.WARNING,
                    "Producing %.0f%% of expected output over the last 24 hours.".formatted(ratio * 100)));
        }
        return List.of();
    }
}
