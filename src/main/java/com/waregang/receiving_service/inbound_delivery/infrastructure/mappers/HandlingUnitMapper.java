package com.waregang.receiving_service.inbound_delivery.infrastructure.mappers;

import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.HandlingUnitJpa;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.InboundDeliveryJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HandlingUnitMapper {

    private final ContentMapper contentMapper;
    private final EntityManager entityManager;

    public HandlingUnit toDomain(HandlingUnitJpa jpa) {
        HandlingUnit domain = HandlingUnit.reconstitute(
                jpa.getId(),
                jpa.getLpn(),
                jpa.getParentUnit() != null ? jpa.getParentUnit().getId() : null,
                jpa.getType(),
                jpa.getInboundDelivery().getId()
        );
        // Manually map child collections
        jpa.getChildUnits().forEach(childJpa -> domain.addChild(toDomain(childJpa)));
        jpa.getContents().forEach(contentJpa -> domain.fillWithContent(contentJpa.getSku(), contentJpa.getQuantity()));
        return domain;
    }

    public HandlingUnitJpa toJpa(HandlingUnit domain) {
        InboundDeliveryJpa deliveryJpa = entityManager.getReference(InboundDeliveryJpa.class, domain.getInboundDeliveryId());
        HandlingUnitJpa jpa = HandlingUnitJpa.create(domain.getLpn(), deliveryJpa);
        
        // Manually map child collections
        domain.getChildUnits().forEach(childDomain -> jpa.addChild(toJpa(childDomain)));
        domain.getContents().forEach(contentDomain -> jpa.fillWithContent(contentDomain.getSku(), contentDomain.getQuantity()));
        return jpa;
    }
}