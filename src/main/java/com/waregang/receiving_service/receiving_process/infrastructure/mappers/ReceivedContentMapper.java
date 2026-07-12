package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedUnitJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceivedContentMapper {

    private final EntityManager entityManager;

    public ReceivedContent toDomain(ReceivedContentJpa jpa) {
        return ReceivedContent.reconstitute(
                jpa.getId(),
                jpa.getSku(),
                jpa.getQuantity(),
                jpa.getContainerUnit().getId()
        );
    }

    public ReceivedContentJpa toJpa(ReceivedContent domain) {
        ReceivedUnitJpa containerUnit = entityManager.getReference(ReceivedUnitJpa.class, domain.getContainerUnitId());
        return new ReceivedContentJpa(
                domain.getId(),
                domain.getSku(),
                domain.getQuantity(),
                containerUnit
        );
    }
}