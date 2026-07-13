package com.waregang.receiving_service.inbound_delivery.infrastructure.mappers;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities.InboundDeliveryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InboundDeliveryMapper {

    private final HandlingUnitMapper handlingUnitMapper;

    public InboundDelivery toDomain(InboundDeliveryJpa jpa) {
        InboundDelivery domain = InboundDelivery.reconstitute(
                jpa.getId(),
                jpa.getExternalId(),
                jpa.getAsnNumber(),
                jpa.getWarehouseId(),
                jpa.getReceivingMode(),
                jpa.getStatus(),
                jpa.getVersion()
        );
        // Manually map child collections
        jpa.getHandlingUnits().forEach(huJpa -> domain.addHandlingUnit(handlingUnitMapper.toDomain(huJpa)));
        return domain;
    }

    public InboundDeliveryJpa toJpa(InboundDelivery domain) {
        InboundDeliveryJpa jpa = InboundDeliveryJpa.create(
                domain.getExternalId(),
                domain.getAsnNumber(),
                domain.getWarehouseId()
        );
        // Manually map child collections
        domain.getHandlingUnits().forEach(huDomain -> jpa.addHandlingUnit(handlingUnitMapper.toJpa(huDomain)));
        return jpa;
    }

    public void updateJpaFromDomain(InboundDeliveryJpa jpa, InboundDelivery domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setWarehouseId(domain.getWarehouseId());
        // collections are accessed through their own repos bc now no need
        // to sync collections when saving this entity
    }
}

