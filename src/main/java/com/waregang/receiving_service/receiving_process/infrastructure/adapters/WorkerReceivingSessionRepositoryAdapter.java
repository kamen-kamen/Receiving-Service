package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.WorkerReceivingSessionRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.WorkerReceivingSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@Repository
public class WorkerReceivingSessionRepositoryAdapter implements WorkerReceivingSessionRepositoryPort {
    private final WorkerReceivingSessionRepositoryJpa repositoryJpa;
    private final WorkerReceivingSessionMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean existsByReceiptIdAndStatus(UUID receiptId, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.existsByReceiptIdAndStatus(receiptId, workerReceivingSessionStatus);
    }

    @Override
    public boolean existsByWorkerIdAndStatus(UUID workerId, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.existsByWorkerIdAndStatus(workerId, workerReceivingSessionStatus);
    }

    //TODO: void or domain entity return ?
    @Override
    public WorkerReceivingSession save(WorkerReceivingSession session) {
        WorkerReceivingSessionJpa saved = repositoryJpa.save(mapper.toJpa(session));

        session.pullDomainEvents().forEach(eventPublisher::publishEvent);
        
        return mapper.toDomain(saved);
    }

    @Override
    public WorkerReceivingSession update(WorkerReceivingSession session) {
        WorkerReceivingSessionJpa sessionJpa = repositoryJpa.findById(session.getId())
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                        .with("session_id", session.getId()));

        mapper.updateJpaFromDomain(sessionJpa, session);

        session.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return mapper.toDomain(sessionJpa);
    }

    @Override
    public Optional<WorkerReceivingSession> findByWorkerIdAndStatus(UUID id, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.findByWorkerIdAndStatus(id, workerReceivingSessionStatus)
                .map(mapper::toDomain);
    }

    @Override
    public Set<WorkerReceivingSession> findAll() {
        return repositoryJpa.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public void flush() {
        repositoryJpa.flush();
    }
}