package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryService;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.api.dto.GetOpenedReceiptsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.infrastructure.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GoodsReceiptService {
    private final InboundDeliveryService inboundDeliveryService;

    private final GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;
    private final WorkerReceivingSessionRepositoryPort workerSessionRepository;

    @Transactional
    public StartReceivingResponse startReceiving(
            StartReceivingRequest request,
            UserPrincipal manager
    ) {
        InboundDelivery inboundDelivery = inboundDeliveryService.findByAsn(request.asnNumber());

        try {
            inboundDelivery.markAsArrived(manager.warehouseId());
        } catch (OptimisticLockException e) {
            throw AppException.of(ReceivingErrorCode.DELIVERY_CONCURRENT_MODIFICATION);
        }

        GoodsReceipt receipt = GoodsReceipt.open(
                manager.id(),
                manager.nickname(),
                inboundDelivery,
                request.gateNumber()
        );

        goodsReceiptRepositoryPort.save(receipt);

        return new StartReceivingResponse(receipt.getId(), inboundDelivery.getReceivingMode());
    }

    @Transactional
    public void closeReceiving(UserPrincipal manager, UUID receiptId) {
        GoodsReceipt receipt = goodsReceiptRepositoryPort.findWithLockById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));

        if (receipt.getStatus() != GoodsReceiptStatus.OPEN) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("expected_status", GoodsReceiptStatus.OPEN)
                    .with("actual_status", receipt.getStatus())
                    .with("receipt_id", receiptId);
        }

        if (workerSessionRepository.existsByReceiptIdAndStatus(
                receiptId,
                WorkerReceivingSessionStatus.IN_PROCESS
        )) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("receipt_id", receiptId)
                    .with("reason", "some workers joined receipt");
        }

        receipt.close();
        goodsReceiptRepositoryPort.update(receipt);
    }

    @Transactional(readOnly = true)
    public GetOpenedReceiptsResponse findAllByStatus(UserPrincipal worker, GoodsReceiptStatus receiptStatus) {
        List<GoodsReceiptDto> receipts = goodsReceiptRepositoryPort.findAllByStatusAndWarehouseId(receiptStatus, worker.warehouseId());
        return new GetOpenedReceiptsResponse(receipts);
    }


    public GoodsReceipt findReceiptByIdWithLock(UUID receiptId) {
        return goodsReceiptRepositoryPort.findWithLockById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));
    }
}