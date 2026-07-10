package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.common.infrastructure.AggregateRoot;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)


public class WorkerReceivingSession extends AggregateRoot {
    private UUID id;
    private UUID workerId;
    private UUID receiptId;
    private UUID inboundDeliveryId;
    private WorkerReceivingSessionStatus status;
    private ReceivingMode receivingMode;
    private String currentUnitLpnPath;
    private UUID currentUnitId;

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
    
    public static WorkerReceivingSession toDomain(
            UUID id,
            UUID workerId,
            UUID receiptId,
            UUID inboundDeliveryId,
            WorkerReceivingSessionStatus status,
            ReceivingMode receivingMode,
            String currentUnitLpnPath,
            UUID currentUnitId
    ) {
        var session = new WorkerReceivingSession();
        session.id = id;
        session.workerId = workerId;
        session.receiptId = receiptId;
        session.inboundDeliveryId = inboundDeliveryId;
        session.status = status;
        session.receivingMode = receivingMode;
        session.currentUnitLpnPath = currentUnitLpnPath;
        session.currentUnitId = currentUnitId;
        return session;
    }

    @Nullable
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

        if (currentUnitLpnPath == null || currentUnitId == null)
            throw AppException.of(ReceivingErrorCode.EMPTY_LPN_NOT_ALLOWED)
                    .with("receiving_mode", this.receivingMode)
                    .with("worker_session_id", this.id);
    }

    public void navigateToUnit(UUID newCurrentUnitId, String lpnSegment) {
        this.currentUnitLpnPath = (this.currentUnitLpnPath == null)
                ? "/" + lpnSegment
                : this.currentUnitLpnPath + "/" + lpnSegment;
        this.currentUnitId = newCurrentUnitId;
    }

    public void navigateBack(@Nullable UUID parentUnitId) {
        if (this.currentUnitId == null) {
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("reason", "Cannot go back, no unit has been scanned yet.");
        }

        int lastSlash = this.currentUnitLpnPath.lastIndexOf('/');
        this.currentUnitLpnPath = lastSlash > 0 ? this.currentUnitLpnPath.substring(0, lastSlash) : null;
        this.currentUnitId = parentUnitId;
    }

    public void complete() {
        if (this.status == WorkerReceivingSessionStatus.COMPLETED) return;
        this.status = WorkerReceivingSessionStatus.COMPLETED;
        this.currentUnitLpnPath = null;
        this.currentUnitId = null;
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
}