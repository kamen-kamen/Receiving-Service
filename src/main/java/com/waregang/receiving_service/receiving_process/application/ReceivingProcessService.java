package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryService;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.infrastructure.*;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.receiving_process.api.dto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor

@Service
public class ReceivingProcessService {
    private final WorkerReceivingSessionRepository workerSessionRepository;
    private final ReceivedUnitRepository receivedUnitRepository;
    private final ReceivedContentRepository receivedContentRepository;

    private final GoodsReceiptService goodsReceiptService;
    private final InboundDeliveryService inboundDeliveryService;

    private final EntityManager entityManager;

    @Transactional
    public JoinReceivingResponse joinReceiving(UserPrincipal worker, UUID receiptId) {
        if (workerSessionRepository.existsByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.IN_PROCESS))
            throw AppException.of(ReceivingErrorCode.WORKER_ALREADY_JOINED);

        GoodsReceipt receipt = goodsReceiptService.findReceiptByIdWithLock(receiptId);
        receipt.ensureAvailableForJoin(worker);

        var newSession = WorkerReceivingSession.createWithBundledWorker(
                worker,
                receipt.getId(),
                receipt.getReceivingMode(),
                receipt.getInboundDelivery().getId()
        );

        saveWorkerReceivingSession(newSession);

        return new JoinReceivingResponse(newSession.getId());
    }

    private void saveWorkerReceivingSession(WorkerReceivingSession session) {
        try {
            workerSessionRepository.save(session);
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() instanceof ConstraintViolationException cve) {
                if ("uk_worker_active_session".equals(cve.getConstraintName())) {
                    throw AppException.of(ReceivingErrorCode.WORKER_ALREADY_JOINED);
                }
            }

            throw e;
        }
    }

    @Transactional
    public ScanHandlingUnitResponse scanHandlingUnit(
            ScanHandlingUnitRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorker(worker);
        session.ensureAvailableForHandlingUnitScan();

        inboundDeliveryService.validateScannedHuAgainstAsn(scanRequest.lpn(), session.getInboundDeliveryId());

        ReceivedUnit proxyParentUnit = null;
        if (session.getCurrentUnit() != null)
            proxyParentUnit = entityManager.getReference(ReceivedUnit.class, session.getCurrentUnit().getId());

        
        ReceivedUnit unit = ReceivedUnit.assignToParentUnit(scanRequest, session, proxyParentUnit);

        saveReceivedUnit(unit);

        session.navigateToUnit(unit);

        return new ScanHandlingUnitResponse(session.getCurrentUnitLpnPath());
    }

    private void saveReceivedUnit(ReceivedUnit unit) {
        try {
            receivedUnitRepository.save(unit);
            receivedUnitRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() instanceof ConstraintViolationException cve) {
                if ("uk_receipt_lpn".equals(cve.getConstraintName())) {
                    throw AppException.of(ReceivingErrorCode.LPN_ALREADY_SCANNED)
                            .with("lpn", unit.getLpn())
                            .with("receiptId", unit.getReceiptId());
                }
            }
            throw e;
        }
    }

    @Transactional
    public ScanContentResponse scanContent(
            ScanContentRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorker(worker);
        session.ensureAvailableForContentScan();

        inboundDeliveryService.validateScannedContentAgainstAsn(
                scanRequest.sku(),
                session.getInboundDeliveryId()
        );

        ReceivedUnit proxyContainer = entityManager.getReference(ReceivedUnit.class, session.getCurrentUnit().getId());
        ReceivedContent content = ReceivedContent.assignToContainer(scanRequest, proxyContainer);

        saveReceivedContent(content);

        return new ScanContentResponse();
    }

    private void saveReceivedContent(ReceivedContent content) {
        try {
            receivedContentRepository.save(content);
            receivedContentRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause() instanceof ConstraintViolationException cve) {
                if ("uk_unit_sku".equals(cve.getConstraintName())) {
                    throw AppException.of(ReceivingErrorCode.DUPLICATE_SKU_SCAN)
                            .with("sku", content.getSku());
                }
            }
            throw e;
        }
    }

    @Transactional
    public NavigationBackResponse getBackToPreviousUnit(
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorker(worker);
        session.navigateBack();

        return new NavigationBackResponse(session.getCurrentUnitLpnPath());
    }


    @Transactional
    public void completeWorkerSession(UserPrincipal worker) {
        WorkerReceivingSession session = findActiveSessionByWorker(worker);
        session.complete();
        workerSessionRepository.save(session);
    }

    private WorkerReceivingSession findActiveSessionByWorker(UserPrincipal worker) {
        return workerSessionRepository
                .findByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.IN_PROCESS)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                        .with("worker_id", worker.id()));
    }
}
