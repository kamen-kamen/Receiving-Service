package com.waregang.receiving_service.receiving_process.domain.ports;

import com.waregang.receiving_service.receiving_process.domain.dto.InboundDeliveryDto;

import java.util.Optional;

public interface InboundDeliveryProvider {
    Optional<InboundDeliveryDto> findByAsn(String asn);
}
