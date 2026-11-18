package com.solardash.model;

import jakarta.persistence.*;
import java.time.Instant;

/** One hourly average: powerKw is what the system produced, expectedKw is the weather-adjusted expectation. */
@Entity
@Table(name = "reading", indexes = @Index(name = "idx_reading_system_ts", columnList = "system_id, ts"))
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system_id")
    private SolarSystem system;
    @Column(name = "ts", nullable = false)
    private Instant timestamp;
    private double powerKw;
    private double expectedKw;

    protected Reading() {}

    public Reading(SolarSystem system, Instant timestamp, double powerKw, double expectedKw) {
        this.system = system;
        this.timestamp = timestamp;
        this.powerKw = powerKw;
        this.expectedKw = expectedKw;
    }

    public Instant getTimestamp() { return timestamp; }
    public double getPowerKw() { return powerKw; }
    public double getExpectedKw() { return expectedKw; }
}
