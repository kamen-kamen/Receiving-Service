package com.waregang.receiving_service.inbound_delivery.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.InboundDeliveryErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryResponse;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.domain.ports.ContentRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.domain.ports.HandlingUnitRepositoryPort;
import com.waregang.receiving_service.inbound_delivery.domain.ports.InboundDeliveryRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InboundDeliveryService {

    private final InboundDeliveryRepositoryPort inboundDeliveryRepositoryPort;
    private final HandlingUnitRepositoryPort handlingUnitRepositoryPort;
    private final ContentRepositoryPort contentRepositoryPort;

    @Transactional
    public CreateDeliveryResponse createDelivery(CreateDeliveryRequest request) {
        InboundDelivery delivery = InboundDelivery.create(request.externalId(), request.asnNumber(), request.warehouseId());
        inboundDeliveryRepositoryPort.save(delivery);

        return new CreateDeliveryResponse(delivery.getId());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public InboundDelivery findByAsn(String asnNumber) {
        return inboundDeliveryRepositoryPort.findByAsnNumber(asnNumber)
                .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                        .with("asn_number", asnNumber));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public InboundDelivery findById(UUID id) {
        return inboundDeliveryRepositoryPort.findById(id)
                .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                        .with("inbound_delivery_id", id));
    }

    @Transactional(readOnly = true)
    public void validateScannedHuAgainstAsn(String scannedLpn, UUID inboundDeliveryId) {
        if (!handlingUnitRepositoryPort.existsByLpnAndInboundDeliveryId(scannedLpn, inboundDeliveryId)) {
            throw AppException.of(ReceivingErrorCode.SCANNED_HU_NOT_IN_THE_DELIVERY)
                    .with("scanned_lpn", scannedLpn)
                    .with("inbound_delivery_id", inboundDeliveryId);
        }
    }

    @Transactional(readOnly = true)
    public void validateScannedContentAgainstAsn(String scannedSku, UUID inboundDeliveryId) {
        if (!contentRepositoryPort.existsBySkuAndInboundDeliveryId(scannedSku, inboundDeliveryId)) {
            throw AppException.of(ReceivingErrorCode.CONTENT_NOT_IN_THE_DELIVERY)
                    .with("scanned_sku", scannedSku)
                    .with("inbound_delivery_id", inboundDeliveryId);
        }
    }

    @Transactional
    public void closeDelivery(ClosedGoodsReceiptEvent event) {
         InboundDelivery delivery = inboundDeliveryRepositoryPort.findById(event.inboundDeliveryId())
                 .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                         .with("inbound_delivery_id", event.inboundDeliveryId()));

         delivery.close();
         inboundDeliveryRepositoryPort.save(delivery);
    }
}