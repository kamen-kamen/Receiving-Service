package com.waregang.receiving_service;

import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import com.waregang.receiving_service.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

/**
 * Object Mother для генерации валидных графов сущностей.
 * Использует реальные доменные методы для сохранения инвариантов.
 */
public class ReceivingTestDataMother {

    // ==========================================
    // 1. SECURITY & USERS
    // ==========================================
    public static UserPrincipalBuilder aManager() {
        return new UserPrincipalBuilder()
                .withNickname("Boss")
                .withAuthority("BOX_MANAGER");
    }

    public static UserPrincipalBuilder aWorker() {
        return new UserPrincipalBuilder()
                .withNickname("HardWorker")
                .withAuthority("BOX_CAT");
    }

    // ==========================================
    // 2. INBOUND DELIVERY (PLAN)
    // ==========================================
    public static InboundDeliveryBuilder aDelivery() {
        return new InboundDeliveryBuilder();
    }

    public static DeliveryWithTreeBuilder aDeliveryWithTree() {
        return new DeliveryWithTreeBuilder();
    }

    // ==========================================
    // 3. RECEIVING PROCESS (FACT)
    // ==========================================
    public static GoodsReceiptBuilder aReceipt(InboundDelivery delivery) {
        return new GoodsReceiptBuilder(delivery);
    }

    public static WorkerSessionBuilder aSession(GoodsReceipt receipt) {
        return new WorkerSessionBuilder(receipt);
    }

    // ==========================================
    // BUILDERS IMPLEMENTATION
    // ==========================================

    public static class UserPrincipalBuilder {
        private UUID id = UUID.randomUUID();
        private String nickname = "test_user";
        private String email = "test@wh.com";
        private String warehouseId = "WH-001";
        private String authority = "BOX_CAT";

        public UserPrincipalBuilder withId(UUID id) { this.id = id; return this; }
        public UserPrincipalBuilder withNickname(String nickname) { this.nickname = nickname; return this; }
        public UserPrincipalBuilder withWarehouseId(String warehouseId) { this.warehouseId = warehouseId; return this; }
        public UserPrincipalBuilder withAuthority(String authority) { this.authority = authority; return this; }

        public UserPrincipal build() {
            return new UserPrincipal(
                    id, nickname, email, warehouseId,
                    List.of(new SimpleGrantedAuthority(authority))
            );
        }
    }

    public static class InboundDeliveryBuilder {
        private String externalId = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        private String asnNumber = "ASN-" + UUID.randomUUID().toString().substring(0, 8);
        private String warehouseId = "WH-001";

        public InboundDeliveryBuilder withAsn(String asn) { this.asnNumber = asn; return this; }
        public InboundDeliveryBuilder withWarehouseId(String whId) { this.warehouseId = whId; return this; }

        public InboundDelivery build() {
            return InboundDelivery.create(externalId, asnNumber, warehouseId);
        }
    }

    public static class DeliveryWithTreeBuilder {
        private InboundDelivery delivery;
        private HandlingUnit pallet;
        private HandlingUnit box;

        public DeliveryWithTreeBuilder() {
            this.delivery = aDelivery().withAsn("ASN-TREE-01").build();
            this.pallet = HandlingUnit.create("PALLET-01", delivery);
            this.box = HandlingUnit.create("BOX-01", delivery);
        }

        public DeliveryWithTreeBuilder withDeliveryId(UUID id) {
            withId(this.delivery, id);
            return this;
        }

        public DeliveryWithTreeBuilder withPalletId(UUID id) {
            withId(this.pallet, id);
            return this;
        }

        public DeliveryWithTreeBuilder withBoxId(UUID id) {
            withId(this.box, id);
            return this;
        }

        public InboundDelivery build() {
            box.fillWithContent("SKU-123", 100L);
            pallet.addChild(box);
            delivery.addHandlingUnit(pallet);
            return delivery;
        }
    }

    public static class GoodsReceiptBuilder {
        private InboundDelivery delivery;
        private UserPrincipal manager = aManager().build();
        private String gateNumber = "GATE-1";

        public GoodsReceiptBuilder(InboundDelivery delivery) {
            this.delivery = delivery;
        }

        public GoodsReceiptBuilder withManager(UserPrincipal manager) { this.manager = manager; return this; }
        public GoodsReceiptBuilder withGate(String gate) { this.gateNumber = gate; return this; }

        public GoodsReceipt build() {
            return GoodsReceipt.open(
                    manager.id(),
                    manager.nickname(),
                    delivery,
                    gateNumber
            );
        }
    }

    public static class WorkerSessionBuilder {
        private UserPrincipal worker = aWorker().build();
        private final GoodsReceipt receipt;

        public WorkerSessionBuilder(GoodsReceipt receipt) {
            this.receipt = receipt;
            this.worker = aWorker().withWarehouseId(receipt.getWarehouseId()).build();
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

    // ==========================================
    // 4. FACTORY HELPERS FOR SCANNED DATA
    // ==========================================

    public static ReceivedUnitJpa scanUnit(WorkerReceivingSession session, ReceivedUnitJpa proxyParent, String lpn) {
        ScanHandlingUnitRequest request = new ScanHandlingUnitRequest(lpn);
        ReceivedUnitJpa unit = ReceivedUnitJpa.assignToParentUnit(request, session, proxyParent);
        session.navigateToUnit(unit); // Обновляем состояние сессии, как это делает сервис
        return unit;
    }

    public static ReceivedContentJpa scanContent(ReceivedUnitJpa currentUnit, String sku, Long quantity) {
        ScanContentRequest request = new ScanContentRequest(sku, quantity);
        return ReceivedContentJpa.assignToContainer(request, currentUnit);
    }

    // ==========================================
    // 5. REFLECTION HELPERS (для тестов БД)
    // ==========================================

    /**
     * Так как ID генерируется внутри, иногда в тестах нужно жестко задать ID.
     */
    public static <T> T withId(T entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
