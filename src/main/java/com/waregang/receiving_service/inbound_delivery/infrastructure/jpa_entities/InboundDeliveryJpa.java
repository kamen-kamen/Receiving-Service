package com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.InboundDeliveryErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDeliveryStatus;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "inbound_deliveries")
public class InboundDeliveryJpa extends AbstractAggregateRoot<InboundDeliveryJpa> implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "external_id", updatable = false, nullable = false, unique = true)
    private String externalId;

    @Column(name = "asn_number", updatable = false, nullable = false, unique = true)
    private String asnNumber;

    @Column(name = "warehouse_id", updatable = false, nullable = false)
    private String warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiving_mode", nullable = false)
    private ReceivingMode receivingMode;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private InboundDeliveryStatus status;

    @OneToMany(
            mappedBy = "inboundDelivery",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final Set<HandlingUnitJpa> handlingUnits = new HashSet<>();

    private InboundDeliveryJpa(String externalId, String asnNumber, String warehouseId) {
        this.id = IdGenerator.generate();
        this.externalId = externalId;
        this.status = InboundDeliveryStatus.EXPECTED;
        this.receivingMode = ReceivingMode.ASN_MATCHING;
        this.asnNumber = asnNumber;
        this.warehouseId = warehouseId;
    }

    public static InboundDeliveryJpa create(String externalId, String asnNumber, String warehouseId) {
        return new InboundDeliveryJpa(externalId, asnNumber, warehouseId);
    }

    public void addHandlingUnit(HandlingUnitJpa unit) {
        if (unit != null && unit.getInboundDelivery() == this) {
            this.handlingUnits.add(unit);
        }
    }

    public void markAsArrived(String managerId) {
        ensureValidForReceiving(managerId);
        this.status = InboundDeliveryStatus.ARRIVED;
    }

    public void ensureValidForReceiving(String managerWarehouseId) {
        if (this.status != InboundDeliveryStatus.EXPECTED) {
            throw AppException.of(InboundDeliveryErrorCode.INVALID_STATE)
                    .with("actual_status", status)
                    .with("expected_status", InboundDeliveryStatus.EXPECTED);
        }

        if (!this.getWarehouseId().equals(managerWarehouseId)) {
            throw AppException.of(ReceivingErrorCode.WAREHOUSE_MISMATCH)
                    .with("actual_wh", managerWarehouseId)
                    .with("expected_wh", this.getWarehouseId());
        }
    }

    public void close() {
        if (this.status != InboundDeliveryStatus.ARRIVED) {
            throw AppException.of(InboundDeliveryErrorCode.DELIVERY_NOT_FOUND)
                    .with("delivery_status", status);
        }

        this.status = InboundDeliveryStatus.CLOSED;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InboundDeliveryJpa other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Version
    private Integer version;

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