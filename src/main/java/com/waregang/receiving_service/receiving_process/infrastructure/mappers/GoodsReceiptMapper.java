package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import org.springframework.stereotype.Component;

@Component
public class GoodsReceiptMapper {

    public GoodsReceipt toDomain(GoodsReceiptJpa jpa) {
        // The fields receivingMode and asnNumber are not persisted in GoodsReceiptJpa,
        // so they are not mapped here. They are only used for creating domain events.
        return GoodsReceipt.reconstitute(
                jpa.getId(),
                jpa.getStatus(),
                jpa.getGateNumber(),
                jpa.getManagerId(),
                jpa.getInboundDeliveryId(),
                jpa.getWarehouseId()
        );
    }

    public GoodsReceiptJpa toJpa(GoodsReceipt domain) {
        return GoodsReceiptJpa.fromDomain(
                domain.getId(),
                domain.getStatus(),
                domain.getGateNumber(),
                domain.getManagerId(),
                domain.getInboundDeliveryId(),
                domain.getWarehouseId()
        );
    }

    public void updateJpaFromDomain(GoodsReceiptJpa jpa, GoodsReceipt domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setGateNumber(domain.getGateNumber());
        jpa.setManagerId(domain.getManagerId());
    }
}