package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AbstractAggregateRoot;
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
public class WorkerReceivingSession extends AbstractAggregateRoot<WorkerReceivingSession> implements Persistable<UUID> {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "worker_id", nullable = false, updatable = false)
    private UUID workerId;

    @Column(name = "receipt_id", nullable = false, updatable = false)
    private UUID receiptId;

    @Column(name = "inbound_delivery_id", nullable = false, updatable = false)
    private UUID inboundDeliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "worker_receiving_session_status", nullable = false)
    private WorkerReceivingSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiving_mode" ,nullable = false, updatable = false)
    private ReceivingMode receivingMode;

    @Nullable
    @Column(name = "current_unit_lpn_path")
    private String currentUnitLpnPath;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_unit_id")
    private ReceivedUnit currentUnit;

    private WorkerReceivingSession(
            UserPrincipal user,
            UUID receiptId,
            ReceivingMode receivingMode,
            UUID inboundDeliveryId
    ) {
        this.id = IdGenerator.generate();
        this.workerId = user.id();
        this.receiptId = receiptId;
        this.status = WorkerReceivingSessionStatus.IN_PROCESS;
        this.receivingMode = receivingMode;
        this.inboundDeliveryId = inboundDeliveryId;
    }

    public static WorkerReceivingSession createWithBundledWorker(
            UserPrincipal user,
            UUID receiptId,
            ReceivingMode receivingMode,
            UUID inboundDeliveryId
    ) {
        return new WorkerReceivingSession(user, receiptId, receivingMode, inboundDeliveryId);
    }

    @Nullable
    @Transient
    public String getCurrentUnitLpn() {
        if (currentUnitLpnPath == null) {
            return null;
        }
        int lastSlash = currentUnitLpnPath.lastIndexOf('/');
        return currentUnitLpnPath.substring(lastSlash + 1);
    }


    public void ensureAvailableForHandlingUnitScan() {
        if (status != WorkerReceivingSessionStatus.IN_PROCESS)
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("expected_status", "in_process");
    }


    public void ensureAvailableForContentScan() {
        if (status != WorkerReceivingSessionStatus.IN_PROCESS)
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("expected_status", WorkerReceivingSessionStatus.IN_PROCESS)
                    .with("worker_session_id", this.id);

        if (currentUnitLpnPath == null || currentUnit == null)
            throw AppException.of(ReceivingErrorCode.EMPTY_LPN_NOT_ALLOWED)
                    .with("receiving_mode", this.receivingMode)
                    .with("worker_session_id", this.id);
    }

    public void navigateToUnit(ReceivedUnit newCurrentUnit) {
        if (this.currentUnitLpnPath == null)
            this.currentUnitLpnPath = "/" + newCurrentUnit.getLpn();
        else
            this.currentUnitLpnPath = this.currentUnitLpnPath + "/" + newCurrentUnit.getLpn();

        this.currentUnit = newCurrentUnit;
    }

    public void navigateBack() {
        if (this.currentUnit == null) {
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("reason", "Cannot go back, no unit has been scanned yet.");
        }
        
        ReceivedUnit parent = this.currentUnit.getParentUnit();
        if (parent == null) {
            throw AppException.of(ReceivingErrorCode.PREVIOUS_UNIT_NOT_FOUND)
                    .with("current_unit_id", this.currentUnit.getId());
        }

        int lastSlash = this.currentUnitLpnPath.lastIndexOf('/');
        this.currentUnitLpnPath = this.currentUnitLpnPath.substring(0, lastSlash);
        this.currentUnit = parent;
    }

    public void complete() {
        if (this.status ==  WorkerReceivingSessionStatus.COMPLETED)
            return;

        this.status = WorkerReceivingSessionStatus.COMPLETED;
        this.currentUnitLpnPath = null;
        this.currentUnit = null;
        registerEvent(new WorkerSessionClosedEvent(this.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerReceivingSession other)) return false;
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