package com.waregang.receiving_service.receiving_process.infrastructure;

import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface WorkerReceivingSessionRepository extends JpaRepository<WorkerReceivingSession, UUID> {
    boolean existsByWorkerIdAndStatus(
            UUID workerId,
            WorkerReceivingSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WorkerReceivingSession> findByWorkerIdAndStatus(
            UUID workerId,
            WorkerReceivingSessionStatus inProcess);

    boolean existsByReceiptIdAndStatus(
            UUID receiptId,
            WorkerReceivingSessionStatus workerReceivingSessionStatus);
}
