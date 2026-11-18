package com.solardash.repo;

import com.solardash.model.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findBySystemIdAndResolvedFalse(Long systemId);

    @Query("select a from Alert a join fetch a.system where a.resolved = false order by a.createdAt desc")
    List<Alert> findOpen();

    @Query("select a from Alert a join fetch a.system order by a.createdAt desc")
    List<Alert> findAllWithSystem();
}
