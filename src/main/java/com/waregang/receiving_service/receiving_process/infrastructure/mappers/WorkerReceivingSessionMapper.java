package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class WorkerReceivingSessionMapper {
    private final EntityManager entityManager;

    public WorkerReceivingSession toDomain(WorkerReceivingSessionJpa jpa) {
        UUID currentUnitId = jpa.getCurrentUnit() != null ? jpa.getCurrentUnit().getId() : null;

        return WorkerReceivingSession.toDomain(
                jpa.getId(),
                jpa.getWorkerId(),
                jpa.getReceiptId(),
                jpa.getInboundDeliveryId(),
                jpa.getStatus(),
                jpa.getReceivingMode(),
                jpa.getCurrentUnitLpnPath(),
                currentUnitId
        );
    }

    public WorkerReceivingSessionJpa toJpa(WorkerReceivingSession domain) {
        ReceivedUnitJpa currentUnit = domain.getCurrentUnitId() != null
                ? entityManager.getReference(ReceivedUnitJpa.class, domain.getCurrentUnitId())
                : null;

        return WorkerReceivingSessionJpa.toJpaEntity(
                domain.getId(),
                domain.getWorkerId(),
                domain.getReceiptId(),
                domain.getInboundDeliveryId(),
                domain.getStatus(),
                domain.getReceivingMode(),
                domain.getCurrentUnitLpnPath(),
                currentUnit
        );
    }

    public void updateJpaFromDomain(WorkerReceivingSessionJpa jpa, WorkerReceivingSession domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setCurrentUnitLpnPath(domain.getCurrentUnitLpnPath());
        jpa.setReceivingMode(domain.getReceivingMode());

        if (domain.getCurrentUnitId() != null) {
            jpa.setCurrentUnit(entityManager.getReference(ReceivedUnitJpa.class, domain.getCurrentUnitId()));
        } else {
            jpa.setCurrentUnit(null);
        }
    }
}