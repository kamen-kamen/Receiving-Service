package com.waregang.receiving_service.receiving_process.event_listener;

import com.waregang.receiving_service.integration.application.InventoryIntegrationService;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor

@Component
public class ReceivingProcessEventListener {
    private final InventoryIntegrationService inventoryIntegrationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkerSessionClosedEvent (WorkerSessionClosedEvent event) {
        inventoryIntegrationService.submitForPutAway(event);
    }





    //  private final CachePort cachePort;


//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void onOpenedGoodsReceiptEvent (OpenedGoodsReceiptEvent event) {
//        PutGoodsReceiptDto dto = new PutGoodsReceiptDto(
//                event.receiptId(),
//                event.asnNumber(),
//                event.gateNumber(),
//                event.warehouseId(),
//                event.managerNickname(),
//                event.mode()
//        );
//
//        // redisRepository.saveReceivingDetails(dto);
//    }

}
