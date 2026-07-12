package com.waregang.receiving_service.inbound_delivery.infrastructure.mappers;

import com.waregang.receiving_service.inbound_delivery.domain.model.Content;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.ContentJpa;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.HandlingUnitJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentMapper {

    private final EntityManager entityManager;

    public Content toDomain(ContentJpa jpa) {
        return Content.reconstitute(
                jpa.getId(),
                jpa.getSku(),
                jpa.getQuantity(),
                jpa.getContainerUnit().getId()
        );
    }

    public ContentJpa toJpa(Content domain) {
        HandlingUnitJpa containerJpa = entityManager.getReference(HandlingUnitJpa.class, domain.getContainerUnitId());
        return new ContentJpa(
                domain.getSku(),
                domain.getQuantity(),
                containerJpa
        );
    }
}