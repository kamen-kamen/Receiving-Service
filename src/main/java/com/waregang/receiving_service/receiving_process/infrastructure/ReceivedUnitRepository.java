package com.waregang.receiving_service.receiving_process.infrastructure;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivedUnitRepository extends JpaRepository<ReceivedUnit, UUID> {
    Optional<ReceivedUnit> findByLpn(@Nullable String currentUnitLpn);

    @Query(value = """
            SELECT ru 
            FROM ReceivedUnit ru 
            WHERE ru.workerSession.id = :workerSessionId 
            AND ru.parentUnit IS NULL
            """)
    List<ReceivedUnit> findAllByWorkerSessionIdAndParentUnitIsNull(UUID workerSessionId);
}
