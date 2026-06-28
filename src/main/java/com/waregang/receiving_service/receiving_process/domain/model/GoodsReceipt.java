package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.event.OpenedGoodsReceiptEvent;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt extends AbstractAggregateRoot<GoodsReceipt> implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, unique = true, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "receiving_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private GoodsReceiptStatus status;

    @Column(name = "gate_number")
    private String gateNumber;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_delivery_id", nullable = false)
    private InboundDelivery inboundDelivery;

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

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
