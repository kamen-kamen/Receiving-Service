package com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "worker_receiving_sessions", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_worker_active_session",
                columnNames = {"worker_id", "worker_receiving_session_status"}
        )
})
public class WorkerReceivingSessionJpa implements Persistable<UUID> {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "worker_id", nullable = false, updatable = false)
    private UUID workerId;

    @Column(name = "receipt_id", nullable = false, updatable = false)
    private UUID receiptId;

    @Column(name = "inbound_delivery_id", nullable = false, updatable = false)
    private UUID inboundDeliveryId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "worker_receiving_session_status", nullable = false)
    private WorkerReceivingSessionStatus status;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "receiving_mode" ,nullable = false, updatable = false)
    private ReceivingMode receivingMode;

    @Setter
    @Nullable
    @Column(name = "current_unit_lpn_path")
    private String currentUnitLpnPath;

    @Setter
    @Nullable
    @Column(name = "current_unit_id")
    private UUID currentUnitId;

    private WorkerReceivingSessionJpa(
            UUID id,
            UUID workerId,
            UUID receiptId,
            UUID inboundDeliveryId,
            WorkerReceivingSessionStatus status,
            ReceivingMode receivingMode,
            @Nullable String currentUnitLpnPath,
            @Nullable UUID currentUnitId
    ) {
        this.id = id;
        this.workerId = workerId;
        this.receiptId = receiptId;
        this.inboundDeliveryId = inboundDeliveryId;
        this.status = status;
        this.receivingMode = receivingMode;
        this.currentUnitLpnPath = currentUnitLpnPath;
        this.currentUnitId = currentUnitId;
    }

    public static WorkerReceivingSessionJpa toJpaEntity(
            UUID id,
            UUID workerId,
            UUID receiptId,
            UUID inboundDeliveryId,
            WorkerReceivingSessionStatus status,
            ReceivingMode receivingMode,
            String currentUnitLpnPath,
            UUID currentUnitId
    ) {
        return new WorkerReceivingSessionJpa(
                id,
                workerId,
                receiptId,
                inboundDeliveryId,
                status,
                receivingMode,
                currentUnitLpnPath,
                currentUnitId
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerReceivingSessionJpa other)) return false;
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