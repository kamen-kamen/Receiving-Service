package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.common.infrastructure.AggregateRoot;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.event.OpenedGoodsReceiptEvent;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class GoodsReceipt extends AggregateRoot {
    private UUID id;
    private GoodsReceiptStatus status;
    private String gateNumber;
    private UUID managerId;
    private InboundDelivery inboundDelivery; // mb it should not be here bc its other BC

    private GoodsReceipt(
            UUID managerId,
            InboundDelivery delivery,
            String gateNumber
    ) {
        this.id = IdGenerator.generate();
        this.status = GoodsReceiptStatus.OPEN;
        this.managerId = managerId;
        this.inboundDelivery = delivery;
        this.gateNumber = gateNumber;
    }

    public static GoodsReceipt open(
            UUID managerId,
            String managerNickname,
            InboundDelivery delivery,
            String gateNumber
    ) {
        GoodsReceipt receipt = new GoodsReceipt(managerId, delivery, gateNumber);
        receipt.registerOpenedEvent(managerNickname);
        return receipt;
    }

    public static GoodsReceipt fromJpa(
            UUID id,
            GoodsReceiptStatus status,
            String gateNumber,
            UUID managerId,
            InboundDelivery inboundDelivery
    ) {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.id = id;
        receipt.status = status;
        receipt.gateNumber = gateNumber;
        receipt.managerId = managerId;
        receipt.inboundDelivery = inboundDelivery;

        return receipt;
    }

    private void registerOpenedEvent(String managerNickname) {
        registerEvent(new OpenedGoodsReceiptEvent(
                this.id,
                this.inboundDelivery.getAsnNumber(),
                this.gateNumber,
                this.inboundDelivery.getWarehouseId(),
                managerNickname,
                this.inboundDelivery.getReceivingMode()
        ));
    }

    public void ensureAvailableForJoin(UserPrincipal worker) {
        if (this.status != GoodsReceiptStatus.OPEN)
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("actual_status", this.status)
                    .with("expected_status", GoodsReceiptStatus.OPEN);

        if (! Objects.equals(worker.warehouseId(), this.getWarehouseId())) {
            throw AppException.of(ReceivingErrorCode.WAREHOUSE_MISMATCH);
        }
    }

    public void close() {
        if (this.status == GoodsReceiptStatus.CLOSED) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("expected_status", GoodsReceiptStatus.OPEN)
                    .with("current_status", GoodsReceiptStatus.CLOSED);
        }

        this.status = GoodsReceiptStatus.CLOSED;

        registerClosedEvent();
    }

    private void registerClosedEvent() {
        registerEvent(new ClosedGoodsReceiptEvent(
                this.id,
                this.inboundDelivery.getId(),
                this.gateNumber
        ));
    }

    public String getWarehouseId() {
        return this.inboundDelivery.getWarehouseId();
    }

    public ReceivingMode getReceivingMode() {
        return this.inboundDelivery.getReceivingMode();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoodsReceipt other)) return false;
        return Objects.equals(this.id, other.id);
    }
}