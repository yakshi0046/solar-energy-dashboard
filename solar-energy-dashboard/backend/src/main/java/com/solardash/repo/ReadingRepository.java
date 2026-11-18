package com.solardash.repo;

import com.solardash.model.Reading;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    List<Reading> findBySystemIdAndTimestampAfterOrderByTimestampAsc(Long systemId, Instant after);
}
