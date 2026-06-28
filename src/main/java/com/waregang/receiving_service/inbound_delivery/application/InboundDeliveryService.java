package com.waregang.receiving_service.inbound_delivery.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.InboundDeliveryErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryResponse;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.ContentRepository;
import com.waregang.receiving_service.inbound_delivery.infrastructure.HandlingUnitRepository;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InboundDeliveryService {

    private final InboundDeliveryRepository inboundDeliveryRepository;
    private final HandlingUnitRepository handlingUnitRepository;
    private final ContentRepository contentRepository;
    private final InboundDeliveryMapper mapper;

    @Transactional
    public CreateDeliveryResponse createDelivery(CreateDeliveryRequest request) {
        InboundDelivery delivery = mapper.toEntity(request);
        inboundDeliveryRepository.save(delivery);

        return new CreateDeliveryResponse(delivery.getId());
    }

    @Transactional
    public InboundDelivery findByAsn(String asnNumber) {
        return inboundDeliveryRepository.findByAsnNumber(asnNumber)
                .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                        .with("asn_number", asnNumber));
    }

    @Transactional(readOnly = true)
    public void validateScannedHuAgainstAsn(String scannedLpn, UUID inboundDeliveryId) {
        if (!handlingUnitRepository.existsByLpnAndInboundDeliveryId(scannedLpn, inboundDeliveryId)) {
            throw AppException.of(ReceivingErrorCode.SCANNED_HU_NOT_IN_THE_DELIVERY)
                    .with("scanned_lpn", scannedLpn)
                    .with("inbound_delivery_id", inboundDeliveryId);
        }
    }

    @Transactional(readOnly = true)
    public void validateScannedContentAgainstAsn(String scannedSku, UUID inboundDeliveryId) {
        if (!contentRepository.existsBySkuAndInboundDeliveryId(scannedSku, inboundDeliveryId)) {
            throw AppException.of(ReceivingErrorCode.CONTENT_NOT_IN_THE_DELIVERY)
                    .with("scanned_sku", scannedSku)
                    .with("inbound_delivery_id", inboundDeliveryId);
        }
    }

    @Transactional
    public void closeDelivery(ClosedGoodsReceiptEvent event) {
         InboundDelivery delivery = inboundDeliveryRepository.findById(event.inboundDeliveryId())
                 .orElseThrow(() -> AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                         .with("inbound_delivery_id", event.inboundDeliveryId()));

         delivery.close();
    }
}
