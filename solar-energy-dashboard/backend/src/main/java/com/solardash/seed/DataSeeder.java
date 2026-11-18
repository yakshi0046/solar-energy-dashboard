package com.solardash.seed;

import com.solardash.model.Reading;
import com.solardash.model.SolarSystem;
import com.solardash.repo.ReadingRepository;
import com.solardash.repo.SolarSystemRepository;
import com.solardash.seed.SyntheticData.SystemSpec;
import com.solardash.service.AlertService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Generates fresh synthetic data on every start, so timestamps always end "now". */
@Component
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SolarSystemRepository systems;
    private final ReadingRepository readings;
    private final AlertService alertService;
    private final int days;

    public DataSeeder(SolarSystemRepository systems, ReadingRepository readings, AlertService alertService,
                      @Value("${app.seed.days:7}") int days) {
        this.systems = systems;
        this.readings = readings;
        this.alertService = alertService;
        this.days = days;
    }

    @Override
    public void run(ApplicationArguments args) {
        Instant end = Instant.now().truncatedTo(ChronoUnit.HOURS);
        List<SystemSpec> specs = SyntheticData.SPECS;
        for (int i = 0; i < specs.size(); i++) {
            SystemSpec spec = specs.get(i);
            SolarSystem saved = systems.save(
                    new SolarSystem(spec.name(), spec.location(), spec.zoneId(), spec.capacityKw()));
            List<Reading> batch = SyntheticData.generate(spec, i, end, days).stream()
                    .map(p -> new Reading(saved, p.timestamp(), p.powerKw(), p.expectedKw()))
                    .toList();
            readings.saveAll(batch);
        }
        int changes = alertService.evaluateAll();
        log.info("Seeded {} systems with {} days of hourly data; {} alert changes", specs.size(), days, changes);
    }
}
