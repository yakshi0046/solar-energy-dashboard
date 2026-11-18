package com.solardash.model;

import jakarta.persistence.*;

@Entity
@Table(name = "solar_system")
public class SolarSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String location;
    private String zoneId;
    private double capacityKw;

    protected SolarSystem() {}

    public SolarSystem(String name, String location, String zoneId, double capacityKw) {
        this.name = name;
        this.location = location;
        this.zoneId = zoneId;
        this.capacityKw = capacityKw;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getZoneId() { return zoneId; }
    public double getCapacityKw() { return capacityKw; }
}
