package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.common.infrastructure.AggregateRoot;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.event.OpenedGoodsReceiptEvent;
import com.waregang.receiving_service.security.UserPrincipal;
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
    private UUID inboundDeliveryId;
    private String warehouseId;

    private GoodsReceipt(
            UUID managerId,
            UUID inboundDeliveryId,
            String warehouseId,
            String gateNumber
    ) {
        this.id = IdGenerator.generate();
        this.status = GoodsReceiptStatus.OPEN;
        this.managerId = managerId;
        this.inboundDeliveryId = inboundDeliveryId;
        this.warehouseId = warehouseId;
        this.gateNumber = gateNumber;
    }

    public static GoodsReceipt open(
            UUID managerId,
            String managerNickname,
            UUID inboundDeliveryId,
            String warehouseId,
            ReceivingMode receivingMode, // Passed for the event
            String asnNumber,           // Passed for the event
            String gateNumber
    ) {
        GoodsReceipt receipt = new GoodsReceipt(managerId, inboundDeliveryId, warehouseId, gateNumber);
        receipt.registerOpenedEvent(managerNickname, asnNumber, receivingMode);
        return receipt;
    }

    public static GoodsReceipt reconstitute(
            UUID id,
            GoodsReceiptStatus status,
            String gateNumber,
            UUID managerId,
            UUID inboundDeliveryId,
            String warehouseId
    ) {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.id = id;
        receipt.status = status;
        receipt.gateNumber = gateNumber;
        receipt.managerId = managerId;
        receipt.inboundDeliveryId = inboundDeliveryId;
        receipt.warehouseId = warehouseId;
        return receipt;
    }

    private void registerOpenedEvent(String managerNickname, String asnNumber, ReceivingMode receivingMode) {
        registerEvent(new OpenedGoodsReceiptEvent(
                this.id,
                asnNumber,
                this.gateNumber,
                this.warehouseId,
                managerNickname,
                receivingMode
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
                this.inboundDeliveryId,
                this.gateNumber
        ));
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