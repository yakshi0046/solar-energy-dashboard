package com.solardash.web;

import com.solardash.model.AlertType;
import com.solardash.model.Severity;
import java.time.Instant;

public final class Dtos {
    private Dtos() {}

    public record FleetSummary(int systems, double totalCapacityKw, double currentPowerKw,
                               double energy24hKwh, int openAlerts) {}

    public record SystemSummary(Long id, String name, String location, String zoneId, double capacityKw,
                                double currentPowerKw, double energy24hKwh, double expected24hKwh,
                                double performanceRatio, String status) {}

    public record ReadingPoint(Instant timestamp, double powerKw, double expectedKw) {}

    public record AlertView(Long id, Long systemId, String systemName, AlertType type, Severity severity,
                            String message, Instant createdAt) {}
}
