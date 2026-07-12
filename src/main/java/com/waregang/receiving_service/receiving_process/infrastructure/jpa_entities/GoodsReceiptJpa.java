package com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "goods_receipts")
public class GoodsReceiptJpa implements Persistable<UUID> {

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

    @Column(name = "inbound_delivery_id", nullable = false)
    private UUID inboundDeliveryId;

    @Column(name = "warehouse_id")
    private String warehouseId;


    private GoodsReceiptJpa(UUID id,
                            GoodsReceiptStatus status,
                            String gateNumber,
                            UUID managerId,
                            UUID inboundDeliveryId,
                            String warehouseId
    ) {
        this.id = id;
        this.status = status;
        this.gateNumber = gateNumber;
        this.managerId = managerId;
        this.inboundDeliveryId = inboundDeliveryId;
        this.warehouseId = warehouseId;
    }

    public static GoodsReceiptJpa fromDomain(UUID id, GoodsReceiptStatus status, String gateNumber, UUID managerId, UUID inboundDeliveryId, String warehouseId) {
        return new GoodsReceiptJpa(id, status, gateNumber, managerId, inboundDeliveryId, warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoodsReceiptJpa other)) return false;
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