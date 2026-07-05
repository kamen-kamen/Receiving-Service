package com.waregang.receiving_service.e2e;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDeliveryStatus;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedContentRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedUnitRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceivingFullFlowIT extends BaseIT {
    @Autowired private ReceivingProcessService receivingProcessService;
    @Autowired private GoodsReceiptService goodsReceiptService;
    @Autowired private InboundDeliveryRepository inboundDeliveryRepository;
    @Autowired private GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;
    @Autowired private WorkerReceivingSessionRepositoryPort workerReceivingSessionRepositoryPort;
    @Autowired private ReceivedUnitRepositoryJpa receivedUnitRepositoryJpa;
    @Autowired private ReceivedContentRepositoryJpa receivedContentRepositoryJpa;

    @Test
    @DisplayName("Full Happy Path: Open Receipt -> Join Worker -> Scan Nested Tree -> Close")
    void fullHappyPath_shouldCompleteSuccessfully() {
        // --- 1. GIVEN ---
        InboundDelivery delivery = DeliveryMother.withNestedTree();
        inboundDeliveryRepository.save(delivery);

        // --- 2. WHEN ---

        // 2.1. Менеджер открывает приёмку по номеру ASN
        var startRequest = new StartReceivingRequest(delivery.getAsnNumber(), "GATE-01");

        var startResponse = goodsReceiptService.startReceiving(startRequest, managerPrincipal);
        UUID receiptId = startResponse.receiptId();

        // 2.2. Воркер подключается (в сервисе создается WorkerReceivingSession)
        receivingProcessService.joinReceiving(workerPrincipal, receiptId);

        // 2.3. Скан паллеты
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("PALLET-01"), workerPrincipal);

        // 2.4. Скан коробки (вложенность должна определиться внутри сервиса по текущему юниту в сессии)
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest("BOX-01"), workerPrincipal);

        // 2.5. Скан товара в последнюю отсканированную коробку
        var scanContentReq = new ScanContentRequest("SKU-123", 100L);
        receivingProcessService.scanContent(scanContentReq, workerPrincipal);

        // 2.6. Воркер завершает свою часть работы
        receivingProcessService.completeWorkerSession(workerPrincipal);

        // 2.7. Менеджер закрывает акт приемки целиком
        goodsReceiptService.closeReceiving(managerPrincipal, receiptId);

        // --- 3. THEN ---


        // Проверяем статус акта
        GoodsReceipt closedReceipt = goodsReceiptRepositoryPort.findById(receiptId)
                .orElseThrow(() -> new AssertionError("GoodsReceipt не найден"));
        assertThat(closedReceipt.getStatus()).isEqualTo(GoodsReceiptStatus.CLOSED);

        // Проверяем статус доставки
        InboundDelivery updatedDelivery = inboundDeliveryRepository.findById(delivery.getId())
                .orElseThrow(() -> new AssertionError("GoodsReceipt не найден"));
        assertThat(updatedDelivery.getStatus()).isEqualTo(InboundDeliveryStatus.CLOSED);

        // Проверяем работу поля version, ответсвеннного за оптимистичную блокировку
        assertThat(updatedDelivery.getVersion()).isEqualTo(2);

        // Проверяем сессию через
        WorkerReceivingSession session = workerReceivingSessionRepositoryPort.findAll().stream()
                .filter(s -> s.getWorkerId().equals(workerPrincipal.id()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Сессия воркера не найдена"));
        assertThat(session.getStatus()).isEqualTo(WorkerReceivingSessionStatus.COMPLETED);
        assertThat(session.getCurrentUnitId()).isNull();
        assertThat(session.getCurrentUnitLpnPath()).isNull();

        // Проверяем, что юниты сохранились и иерархия верна
        ReceivedUnitJpa savedPallet = receivedUnitRepositoryJpa.findByLpn("PALLET-01")
                .orElseThrow(() -> new AssertionError("Паллета не найдена в БД"));
        ReceivedUnitJpa savedBox = receivedUnitRepositoryJpa.findByLpn("BOX-01")
                .orElseThrow(() -> new AssertionError("Коробка не найдена в БД"));

        //  BOX-01 должен ссылаться на PALLET-01
        assertThat(savedBox.getParentUnit()).isNotNull();
        assertThat(savedBox.getParentUnit().getId()).isEqualTo(savedPallet.getId());

        // Проверяем контент
        List<ReceivedContentJpa> contents = receivedContentRepositoryJpa.findAll();
        assertThat(contents).hasSize(1);
        assertThat(contents.getFirst().getSku()).isEqualTo("SKU-123");
        assertThat(contents.getFirst().getQuantity()).isEqualTo(100);
        assertThat(contents.getFirst().getContainerUnit().getId()).isEqualTo(savedBox.getId());
    }
}