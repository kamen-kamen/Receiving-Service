package com.waregang.receiving_service.receiving_process.event_listener;

import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryService;
import com.waregang.receiving_service.integration.application.DiscrepanciesReportService;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor

@Component
public class GoodsReceiptEventListener {
    private final DiscrepanciesReportService reportService;
    private final InboundDeliveryService deliveryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoodsReceiptClosedEvent (ClosedGoodsReceiptEvent event) {
        reportService.processClosedEvent(event);
        deliveryService.closeDelivery(event);
    }
}
