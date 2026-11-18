package com.solardash.repo;

import com.solardash.model.SolarSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolarSystemRepository extends JpaRepository<SolarSystem, Long> {}
