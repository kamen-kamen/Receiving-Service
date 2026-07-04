package com.waregang.receiving_service.infrastucture;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.fixtures.delivery.InboundDeliveryBuilder;
import com.waregang.receiving_service.fixtures.receiving.ReceivingMother;
import com.waregang.receiving_service.fixtures.receiving.WorkerSessionBuilder;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.GoodsReceiptRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedContentRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedUnitRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.WorkerReceivingSessionRepositoryJpa;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryQueryIT extends BaseIT {

    @Autowired private InboundDeliveryRepository deliveryRepository;
    @Autowired private GoodsReceiptRepositoryPort receiptRepository;
    @Autowired private ReceivedContentRepositoryJpa contentRepository;
    @Autowired private WorkerReceivingSessionRepositoryPort sessionRepository;
    @Autowired private ReceivedUnitRepositoryJpa receivedUnitRepositoryJpa;
    @Autowired private ReceivedContentRepositoryJpa receivedContentRepositoryJpa;
    @PersistenceContext EntityManager entityManager;

    private InboundDelivery delivery;
    private GoodsReceipt receipt;
    private WorkerReceivingSession session;
    private UUID receiptId;

    @BeforeEach
    void setUp() {
        delivery = DeliveryMother.withNestedTree();
        deliveryRepository.save(delivery);

        receipt = ReceivingMother.receipt(delivery);
        receiptRepository.save(receipt);
        receiptId = receipt.getId();

        session = WorkerSessionBuilder.aSession(receipt)
                .withWorker(workerPrincipal)
                .build();
        sessionRepository.save(session);
    }

    // =============================================
    // findActualSkuQuantities
    // =============================================

    @Test
    @DisplayName("Should return aggregated SKU quantities for receipt")
    void shouldReturnAggregatedSkuQuantities() {
        ReceivedUnitJpa pallet = saveUnit("PALLET-01", null);
        ReceivedUnitJpa box = saveUnit("BOX-01", pallet);
        saveContent(box, "SKU-1", 10L);
        saveContent(box, "SKU-1", 5L); // тот же SKU, должен суммироваться
        saveContent(box, "SKU-2", 20L);

        List<SkuQuantityDto> result = contentRepository.findActualSkuQuantitiesByReceiptId(receiptId);

        Map<String, Long> bySku = result.stream()
                .collect(Collectors.toMap(SkuQuantityDto::sku, SkuQuantityDto::quantity));

        assertThat(bySku).containsEntry("SKU-1", 15L);
        assertThat(bySku).containsEntry("SKU-2", 20L);
    }

    @Test
    @DisplayName("Should return empty list when nothing scanned")
    void shouldReturnEmptyWhenNothingScanned() {
        List<SkuQuantityDto> result = contentRepository.findActualSkuQuantitiesByReceiptId(receiptId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not mix quantities between different receipts")
    void shouldNotMixQuantitiesBetweenReceipts() {
        // Создаем второго, уникального воркера для этого теста
        var otherWorkerRequest = new RegisterUserRequest("other_worker", "WH-001", "other_worker@test.com", "password");
        User otherWorker = User.createBoxCat(otherWorkerRequest, passwordEncoder.encode(otherWorkerRequest.password()));
        userRepository.save(otherWorker);
        UserPrincipal otherWorkerPrincipal = UserPrincipal.from(otherWorker);

        // Другая приёмка с другой сессией
        InboundDelivery otherDelivery = InboundDeliveryBuilder.aDelivery()
                .withAsn("ASN-OTHER-01")
                .build();
        deliveryRepository.save(otherDelivery);

        GoodsReceipt otherReceipt = ReceivingMother.receipt(otherDelivery);
        receiptRepository.save(otherReceipt);

        WorkerReceivingSession otherSession = WorkerSessionBuilder.aSession(otherReceipt)
                .withWorker(otherWorkerPrincipal)
                .build();
        sessionRepository.save(otherSession);

        // Сканируем в обе приёмки
        ReceivedUnitJpa box1 = saveUnit("BOX-RECEIPT-1", null);
        saveContent(box1, "SKU-1", 100L);

        ReceivedUnitJpa box2 = saveUnitForSession("BOX-RECEIPT-2", null, otherSession);
        saveContentForUnit(box2, "SKU-1", 999L);

        List<SkuQuantityDto> result = contentRepository.findActualSkuQuantitiesByReceiptId(receiptId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().quantity()).isEqualTo(100L);
    }

    // =============================================
    // findAllByWorkerSessionIdAndParentUnitIsNull
    // =============================================

    @Test
    @DisplayName("Should return only root units for session")
    void shouldReturnOnlyRootUnits() {
        ReceivedUnitJpa pallet = saveUnit("PALLET-01", null);
        saveUnit("BOX-01", pallet); // дочерний — не должен попасть

        List<ReceivedUnitJpa> roots = receivedUnitRepositoryJpa
                .findAllByWorkerSessionIdAndParentUnitIsNull(session.getId());

        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().getLpn()).isEqualTo("PALLET-01");
    }

    @Test
    @DisplayName("Should load children ")
    @Transactional
    void shouldLoadChildrenEagerly() {
        ReceivedUnitJpa pallet = saveUnit("PALLET-01", null);
        ReceivedUnitJpa box = saveUnit("BOX-01", pallet);
        saveContent(box, "SKU-1", 10L);

        // otherwise hibernate uses L1 cache and loadedPallet.childs is empty bc we do not add in Java memory - only save in db
        entityManager.flush();
        entityManager.clear();

        List<ReceivedUnitJpa> roots = receivedUnitRepositoryJpa
                .findAllByWorkerSessionIdAndParentUnitIsNull(session.getId());

        ReceivedUnitJpa loadedPallet = roots.getFirst();
        assertThat(loadedPallet.getChildUnits()).hasSize(1);
        assertThat(loadedPallet.getChildUnits().stream().toList().getFirst().getContents()).hasSize(1);
    }
    // =============================================
    // Helpers
    // =============================================

    private ReceivedUnitJpa saveUnit(String lpn, ReceivedUnitJpa parent) {
        return saveUnitForSession(lpn, parent, session);
    }

    private ReceivedUnitJpa saveUnitForSession(String lpn, ReceivedUnitJpa parent, WorkerReceivingSession s) {
        ReceivedUnitJpa unit = ReceivedUnitJpa.assignToParentUnit(
                new ScanHandlingUnitRequest(lpn),
                s,
                parent
        );
        return receivedUnitRepositoryJpa.save(unit);
    }

    private ReceivedContentJpa saveContent(ReceivedUnitJpa unit, String sku, long qty) {
        return saveContentForUnit(unit, sku, qty);
    }

    private ReceivedContentJpa saveContentForUnit(ReceivedUnitJpa unit, String sku, long qty) {
        ReceivedContentJpa content = ReceivedContentJpa.assignToContainer(
                new ScanContentRequest(sku, qty),
                unit
        );
        return receivedContentRepositoryJpa.save(content);
    }
}