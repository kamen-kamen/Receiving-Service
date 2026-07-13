package com.waregang.receiving_service.inbound_delivery.domain.ports;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InboundDeliveryRepositoryPort {
    InboundDelivery save(InboundDelivery delivery);

    Optional<InboundDelivery> findByAsnNumber(String asn);

    Optional<InboundDelivery> findById(UUID id);

    InboundDelivery update(InboundDelivery delivery);

    List<SkuQuantityDto> findExpectedSkuQuantities(UUID deliveryId);
}
