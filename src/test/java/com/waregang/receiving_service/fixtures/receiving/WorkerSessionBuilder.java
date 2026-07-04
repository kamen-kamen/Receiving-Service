package com.waregang.receiving_service.fixtures.receiving;

import com.waregang.receiving_service.fixtures.user.UserMother;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import com.waregang.receiving_service.security.UserPrincipal;

public class WorkerSessionBuilder {
    private UserPrincipal worker;
    private final GoodsReceipt receipt;

    public static WorkerSessionBuilder aSession(GoodsReceipt receipt) {
        return new WorkerSessionBuilder(receipt);
    }

    private WorkerSessionBuilder(GoodsReceipt receipt) {
        this.receipt = receipt;
        this.worker = UserMother.worker(receipt.getWarehouseId());
    }

    public WorkerSessionBuilder withWorker(UserPrincipal worker) { this.worker = worker; return this; }

    public WorkerReceivingSession build() {
        return WorkerReceivingSession.createWithBundledWorker(
                worker,
                receipt.getId(),
                receipt.getReceivingMode(),
                receipt.getInboundDelivery().getId()
        );
    }
}
