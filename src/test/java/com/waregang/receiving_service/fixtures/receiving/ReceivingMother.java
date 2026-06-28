package com.waregang.receiving_service.fixtures.receiving;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;

public class ReceivingMother {
    
    public static GoodsReceipt receipt(InboundDelivery delivery) {
        return GoodsReceiptBuilder.aReceipt(delivery).build();
    }

    public static WorkerReceivingSession session(GoodsReceipt receipt) {
        return WorkerSessionBuilder.aSession(receipt).build();
    }
}
