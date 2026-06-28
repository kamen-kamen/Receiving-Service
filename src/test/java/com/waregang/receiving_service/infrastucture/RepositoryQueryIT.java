package com.waregang.receiving_service.infrastucture;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.fixtures.delivery.DeliveryMother;
import com.waregang.receiving_service.fixtures.delivery.InboundDeliveryBuilder;
import com.waregang.receiving_service.fixtures.receiving.ReceivingMother;
import com.waregang.receiving_service.fixtures.user.UserMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.infrastructure.InboundDeliveryRepository;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.infrastructure.GoodsReceiptRepository;
import com.waregang.receiving_service.receiving_process.infrastructure.ReceivedContentRepository;
import com.waregang.receiving_service.receiving_process.infrastructure.ReceivedUnitRepository;
import com.waregang.receiving_service.receiving_process.infrastructure.WorkerReceivingSessionRepository;
import com.waregang.receiving_service.integration.infrastrusture.dto.SkuQuantityDto;
import com.waregang.receiving_service.security.UserPrincipal;
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
    @Autowired private GoodsReceiptRepository receiptRepository;
    @Autowired private ReceivedContentRepository contentRepository;
    @Autowired private WorkerReceivingSessionRepository sessionRepository;
    @Autowired private ReceivedUnitRepository receivedUnitRepository;
    @Autowired private ReceivedContentRepository receivedContentRepository;
    @PersistenceContext EntityManager entityManager;

    private InboundDelivery delivery;
    private GoodsReceipt receipt;
    private WorkerReceivingSession session;
    private UUID receiptId;

    @BeforeEach
    void setUp() {
        delivery = DeliveryMother.withNestedTree();
        deliveryRepository.save(delivery);

        UserPrincipal manager = UserMother.manager();
        UserPrincipal worker = UserMother.worker(delivery.getWarehouseId());

        receipt = ReceivingMother.receipt(delivery);
        receiptRepository.save(receipt);
        receiptId = receipt.getId();

        session = ReceivingMother.session(receipt);
        sessionRepository.save(session);
    }

    // =============================================
    // findActualSkuQuantities
    // =============================================

    @Test
    @DisplayName("Should return aggregated SKU quantities for receipt")
    void shouldReturnAggregatedSkuQuantities() {
        ReceivedUnit pallet = saveUnit("PALLET-01", null);
        ReceivedUnit box = saveUnit("BOX-01", pallet);
        saveContent(box, "SKU-1", 10);
        saveContent(box, "SKU-1", 5); // тот же SKU, должен суммироваться
        saveContent(box, "SKU-2", 20);

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
        // Другая приёмка с другой сессией
        InboundDelivery otherDelivery = InboundDeliveryBuilder.aDelivery()
                .withAsn("ASN-OTHER-01")
                .build();
        deliveryRepository.save(otherDelivery);

        GoodsReceipt otherReceipt = ReceivingMother.receipt(otherDelivery);
        receiptRepository.save(otherReceipt);

        WorkerReceivingSession otherSession = ReceivingMother.session(otherReceipt);
        sessionRepository.save(otherSession);

        // Сканируем в обе приёмки
        ReceivedUnit box1 = saveUnit("BOX-RECEIPT-1", null);
        saveContent(box1, "SKU-1", 100);

        ReceivedUnit box2 = saveUnitForSession("BOX-RECEIPT-2", null, otherSession);
        saveContentForUnit(box2, "SKU-1", 999);

        List<SkuQuantityDto> result = contentRepository.findActualSkuQuantitiesByReceiptId(receiptId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().quantity()).isEqualTo(100);
    }

    // =============================================
    // findAllByWorkerSessionIdAndParentUnitIsNull
    // =============================================

    @Test
    @DisplayName("Should return only root units for session")
    void shouldReturnOnlyRootUnits() {
        ReceivedUnit pallet = saveUnit("PALLET-01", null);
        saveUnit("BOX-01", pallet); // дочерний — не должен попасть

        List<ReceivedUnit> roots = receivedUnitRepository
                .findAllByWorkerSessionIdAndParentUnitIsNull(session.getId());

        assertThat(roots).hasSize(1);
        assertThat(roots.getFirst().getLpn()).isEqualTo("PALLET-01");
    }

    @Test
    @DisplayName("Should load children ")
    @Transactional
    void shouldLoadChildrenEagerly() {
        ReceivedUnit pallet = saveUnit("PALLET-01", null);
        ReceivedUnit box = saveUnit("BOX-01", pallet);
        saveContent(box, "SKU-1", 10);

        // otherwise hibernate uses L1 cache and loadedPallet.childs is empty bc we do not add in Java memory - only save in db
        entityManager.flush();
        entityManager.clear();

        List<ReceivedUnit> roots = receivedUnitRepository
                .findAllByWorkerSessionIdAndParentUnitIsNull(session.getId());

        ReceivedUnit loadedPallet = roots.getFirst();
        assertThat(loadedPallet.getChildUnits()).hasSize(1);
        assertThat(loadedPallet.getChildUnits().stream().toList().getFirst().getContents()).hasSize(1);
    }
    // =============================================
    // Helpers
    // =============================================

    private ReceivedUnit saveUnit(String lpn, ReceivedUnit parent) {
        return saveUnitForSession(lpn, parent, session);
    }

    private ReceivedUnit saveUnitForSession(String lpn, ReceivedUnit parent, WorkerReceivingSession s) {
        ReceivedUnit unit = ReceivedUnit.assignToParentUnit(
                new ScanHandlingUnitRequest(lpn),
                s,
                parent
        );
        return receivedUnitRepository.save(unit);
    }

    private ReceivedContent saveContent(ReceivedUnit unit, String sku, int qty) {
        return saveContentForUnit(unit, sku, qty);
    }

    private ReceivedContent saveContentForUnit(ReceivedUnit unit, String sku, int qty) {
        ReceivedContent content = ReceivedContent.assignToContainer(
                new ScanContentRequest(sku, qty),
                unit
        );
        return receivedContentRepository.save(content);
    }
}
