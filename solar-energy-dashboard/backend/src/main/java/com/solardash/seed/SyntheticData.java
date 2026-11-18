package com.solardash.seed;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deterministic synthetic solar data. "Expected" is weather-adjusted (as if derived from irradiance data),
 * so a healthy system tracks it closely on cloudy days too. Faults are applied only to "actual".
 */
public final class SyntheticData {
    public record SystemSpec(String name, String location, String zoneId, double capacityKw,
                             int faultHours, double faultFactor) {}

    public record Point(Instant timestamp, double powerKw, double expectedKw) {}

    /** faultHours = how many of the latest hours are affected; faultFactor = share of expected output actually produced. */
    public static final List<SystemSpec> SPECS = List.of(
            new SystemSpec("Maple Street Residence", "Phoenix, USA", "America/Phoenix", 7.2, 0, 1.0),
            new SystemSpec("Harbor Warehouse", "Sydney, Australia", "Australia/Sydney", 120, 30, 0.45),
            new SystemSpec("Green Valley School", "Pune, India", "Asia/Kolkata", 45, 0, 1.0),
            new SystemSpec("Desert Ridge Farm", "Fresno, USA", "America/Los_Angeles", 250, 30, 0.0),
            new SystemSpec("Alpine Community Hall", "Zurich, Switzerland", "Europe/Zurich", 18, 30, 0.72),
            new SystemSpec("Lakeside Clinic", "Nairobi, Kenya", "Africa/Nairobi", 30, 0, 1.0));

    private static final double SYSTEM_DERATE = 0.85;

    private SyntheticData() {}

    public static List<Point> generate(SystemSpec spec, int index, Instant end, int days) {
        ZoneId zone = ZoneId.of(spec.zoneId());
        Random noise = new Random(7_000L + index);
        List<Point> out = new ArrayList<>();
        for (int i = days * 24; i >= 0; i--) {
            Instant ts = end.minus(i, ChronoUnit.HOURS);
            ZonedDateTime local = ts.atZone(zone);
            double hour = local.getHour() + 0.5;
            double sun = (hour < 6 || hour > 18) ? 0 : Math.sin(Math.PI * (hour - 6) / 12);
            double seasonal = 0.9 + 0.1 * Math.sin(2 * Math.PI * (local.getDayOfYear() - 80) / 365.0);
            double cloud = cloudFactor(index, local.toLocalDate().toEpochDay());
            double expected = spec.capacityKw() * SYSTEM_DERATE * sun * seasonal * cloud;
            double fault = i < spec.faultHours() ? spec.faultFactor() : 1.0;
            double jitter = 1 + Math.max(-0.08, Math.min(0.08, noise.nextGaussian() * 0.03));
            out.add(new Point(ts, round(expected * fault * jitter), round(expected)));
        }
        return out;
    }

    /** Daily cloudiness between 0.70 and 1.00, from a hash so neighbouring days are uncorrelated. */
    static double cloudFactor(int index, long epochDay) {
        long z = 31L * index + epochDay;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        double unit = (z >>> 11) * (1.0 / (1L << 53));
        return 0.70 + 0.30 * unit;
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
