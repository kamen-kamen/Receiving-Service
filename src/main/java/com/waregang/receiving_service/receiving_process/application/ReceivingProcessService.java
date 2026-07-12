package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.api.dto.*;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.domain.ports.InboundDeliveryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReceivingProcessService {
    private final WorkerReceivingSessionRepositoryPort workerSessionRepository;
    private final ReceivedUnitRepositoryPort receivedUnitRepository;
    private final ReceivedContentRepositoryPort receivedContentRepository;
    private final GoodsReceiptService goodsReceiptService;
    private final InboundDeliveryPort inboundDeliveryPort;

    @Transactional
    public JoinReceivingResponse joinReceiving(UserPrincipal worker, UUID receiptId) {
        if (workerSessionRepository.existsByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.IN_PROCESS))
            throw AppException.of(ReceivingErrorCode.WORKER_ALREADY_JOINED);

        GoodsReceipt receipt = goodsReceiptService.findReceiptByIdWithLock(receiptId);
        receipt.ensureAvailableForJoin(worker);

        InboundDelivery inboundDelivery = inboundDeliveryPort.findById(receipt.getInboundDeliveryId());

        var newSession = WorkerReceivingSession.createWithBundledWorker(
                worker,
                receipt.getId(),
                inboundDelivery.getReceivingMode(),
                inboundDelivery.getId()
        );

        WorkerReceivingSession savedSession = workerSessionRepository.save(newSession);

        return new JoinReceivingResponse(savedSession.getId());
    }

    @Transactional
    public ScanHandlingUnitResponse scanHandlingUnit(
            ScanHandlingUnitRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorkerWithLock(worker);
        session.ensureAvailableForHandlingUnitScan();

        inboundDeliveryPort.validateScannedHuAgainstAsn(scanRequest.lpn(), session.getInboundDeliveryId());

        ReceivedUnit unit = ReceivedUnit.create(
                scanRequest.lpn(),
                session.getCurrentUnitId(),
                session.getId(),
                session.getReceiptId()
        );
  
        receivedUnitRepository.save(unit);

        session.navigateToUnit(unit.getId(), unit.getLpn());
        workerSessionRepository.update(session);

        return new ScanHandlingUnitResponse(session.getCurrentUnitLpnPath());
    }

    @Transactional
    public ScanContentResponse scanContent(
            ScanContentRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorkerWithLock(worker);
        session.ensureAvailableForContentScan();

        inboundDeliveryPort.validateScannedContentAgainstAsn(
                scanRequest.sku(),
                session.getInboundDeliveryId()
        );

        ReceivedContent content = ReceivedContent.create(
                scanRequest.sku(),
                scanRequest.quantity(),
                session.getCurrentUnitId()
        );

        receivedContentRepository.save(content);

        return new ScanContentResponse();
    }

    @Transactional
    public NavigationBackResponse getBackToPreviousUnit(UserPrincipal worker) {
        WorkerReceivingSession session = findActiveSessionByWorkerWithLock(worker);

        UUID parentUnitId = receivedUnitRepository.findById(session.getCurrentUnitId())
                .map(ReceivedUnit::getParentUnitId)
                .orElse(null);

        session.navigateBack(parentUnitId);
        workerSessionRepository.update(session);

        return new NavigationBackResponse(session.getCurrentUnitLpnPath());
    }

    @Transactional
    public void completeWorkerSession(UserPrincipal worker) {
        WorkerReceivingSession session = findActiveSessionByWorkerWithLock(worker);
        session.complete();
        workerSessionRepository.update(session);
    }

    private WorkerReceivingSession findActiveSessionByWorkerWithLock(UserPrincipal worker) {
        return workerSessionRepository
                .findByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.IN_PROCESS)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                        .with("worker_id", worker.id()));
    }
}