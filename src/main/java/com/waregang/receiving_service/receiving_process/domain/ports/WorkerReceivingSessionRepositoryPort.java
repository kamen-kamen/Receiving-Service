package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface WorkerReceivingSessionRepositoryPort {
    boolean existsByReceiptIdAndStatus(UUID receiptId, WorkerReceivingSessionStatus workerReceivingSessionStatus);
    boolean existsByWorkerIdAndStatus(UUID id, WorkerReceivingSessionStatus workerReceivingSessionStatus);
    WorkerReceivingSession save(WorkerReceivingSession session);
    WorkerReceivingSession update(WorkerReceivingSession session);
    Optional<WorkerReceivingSession> findByWorkerIdAndStatus(UUID id, WorkerReceivingSessionStatus workerReceivingSessionStatus);
    Set<WorkerReceivingSession> findAll();
    void flush();
}