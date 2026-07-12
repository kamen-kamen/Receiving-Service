package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;

import java.util.UUID;

public interface InboundDeliveryPort {
    InboundDelivery findById(UUID id);
    void validateScannedHuAgainstAsn(String scannedLpn, UUID inboundDeliveryId);
    void validateScannedContentAgainstAsn(String scannedSku, UUID inboundDeliveryId);
}
