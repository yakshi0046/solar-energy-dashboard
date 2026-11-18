package com.solardash.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alert")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system_id")
    private SolarSystem system;
    @Enumerated(EnumType.STRING)
    private AlertType type;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    private String message;
    private Instant createdAt;
    private boolean resolved;

    protected Alert() {}

    public Alert(SolarSystem system, AlertType type, Severity severity, String message) {
        this.system = system;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public SolarSystem getSystem() { return system; }
    public AlertType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isResolved() { return resolved; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setMessage(String message) { this.message = message; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
