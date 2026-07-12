package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryService;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.domain.ports.InboundDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InboundDeliveryAdapter implements InboundDeliveryPort {
    private final InboundDeliveryService inboundDeliveryService;

    @Override
    public InboundDelivery findById(UUID id) {
        return inboundDeliveryService.findById(id);
    }

    @Override
    public void validateScannedHuAgainstAsn(String scannedLpn, UUID inboundDeliveryId) {
        inboundDeliveryService.validateScannedHuAgainstAsn(scannedLpn, inboundDeliveryId);
    }

    @Override
    public void validateScannedContentAgainstAsn(String scannedSku, UUID inboundDeliveryId) {
        inboundDeliveryService.validateScannedContentAgainstAsn(scannedSku, inboundDeliveryId);
    }
}
