package com.waregang.receiving_service.inbound_delivery.infrastructure.adapters;

import com.waregang.receiving_service.inbound_delivery.domain.ports.InboundDeliveryRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.dto.InboundDeliveryDto;
import com.waregang.receiving_service.receiving_process.domain.ports.InboundDeliveryProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InboundDeliveryProviderAdapter implements InboundDeliveryProvider {

    private final InboundDeliveryRepositoryPort inboundDeliveryRepositoryPort;

    @Override
    public Optional<InboundDeliveryDto> findByAsn(String asn) {
        return inboundDeliveryRepositoryPort.findByAsnNumber(asn)
                .map(delivery -> new InboundDeliveryDto(
                        delivery.getId(),
                        delivery.getAsnNumber(),
                        delivery.getWarehouseId(),
                        delivery.getReceivingMode()
                ));
    }
}