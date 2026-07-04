package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.infrastructure.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import org.springframework.stereotype.Component;

@Component
public class GoodsReceiptMapper {

    public GoodsReceipt toDomain(GoodsReceiptJpa jpa) {
        return GoodsReceipt.fromJpa(
                jpa.getId(),
                jpa.getStatus(),
                jpa.getGateNumber(),
                jpa.getManagerId(),
                jpa.getInboundDelivery()
        );
    }

    public GoodsReceiptJpa toJpa(GoodsReceipt domain) {
        return GoodsReceiptJpa.fromDomain(
                domain.getId(),
                domain.getStatus(),
                domain.getGateNumber(),
                domain.getManagerId(),
                domain.getInboundDelivery()
        );
    }

    public void updateJpaFromDomain(GoodsReceiptJpa jpa, GoodsReceipt domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setGateNumber(domain.getGateNumber());
        jpa.setManagerId(domain.getManagerId());
    }

    public GoodsReceiptDto toGoodsReceiptDto(GoodsReceipt receipt) {
        return new GoodsReceiptDto(
                receipt.getId(),
                receipt.getStatus(),
                receipt.getWarehouseId(),
                receipt.getGateNumber(),
                receipt.getManagerId(),
                receipt.getReceivingMode(),
                receipt.getInboundDelivery().getId()
        );
    }

    public GoodsReceiptDto toGoodsReceiptDto(GoodsReceiptJpa jpa) {
        return new GoodsReceiptDto(
                jpa.getId(),
                jpa.getStatus(),
                jpa.getWarehouseId(),
                jpa.getGateNumber(),
                jpa.getManagerId(),
                jpa.getReceivingMode(),
                jpa.getInboundDelivery().getId()
        );
    }
}