package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedUnitJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceivedUnitMapper {

    private final EntityManager entityManager;

    public ReceivedUnit toDomain(ReceivedUnitJpa jpa) {
        return ReceivedUnit.reconstitute(
                jpa.getId(),
                jpa.getLpn(),
                jpa.getParentUnit() != null ? jpa.getParentUnit().getId() : null,
                jpa.getWorkerSessionId(),
                jpa.getReceiptId()
        );
    }

    public ReceivedUnitJpa toJpa(ReceivedUnit domain) {
        ReceivedUnitJpa parentJpa = null;
        if (domain.getParentUnitId() != null) {
            parentJpa = entityManager.getReference(ReceivedUnitJpa.class, domain.getParentUnitId());
        }

        return new ReceivedUnitJpa(
                domain.getId(),
                domain.getLpn(),
                parentJpa,
                domain.getWorkerSessionId(),
                domain.getReceiptId()
        );
    }
}