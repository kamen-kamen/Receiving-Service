package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkerReceivingSessionMapper {

    public WorkerReceivingSession toDomain(WorkerReceivingSessionJpa jpa) {
        return WorkerReceivingSession.reconstitute(
                jpa.getId(),
                jpa.getWorkerId(),
                jpa.getReceiptId(),
                jpa.getInboundDeliveryId(),
                jpa.getStatus(),
                jpa.getReceivingMode(),
                jpa.getCurrentUnitLpnPath(),
                jpa.getCurrentUnitId()
        );
    }

    public WorkerReceivingSessionJpa toJpa(WorkerReceivingSession domain) {
        return WorkerReceivingSessionJpa.toJpaEntity(
                domain.getId(),
                domain.getWorkerId(),
                domain.getReceiptId(),
                domain.getInboundDeliveryId(),
                domain.getStatus(),
                domain.getReceivingMode(),
                domain.getCurrentUnitLpnPath(),
                domain.getCurrentUnitId()
        );
    }

    public void updateJpaFromDomain(WorkerReceivingSessionJpa jpa, WorkerReceivingSession domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setCurrentUnitLpnPath(domain.getCurrentUnitLpnPath());
        jpa.setCurrentUnitId(domain.getCurrentUnitId());
    }
}