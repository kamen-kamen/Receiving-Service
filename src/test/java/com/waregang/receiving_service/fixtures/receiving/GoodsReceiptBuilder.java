package com.waregang.receiving_service.fixtures.receiving;

import com.waregang.receiving_service.fixtures.user.UserMother;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.security.UserPrincipal;

public class GoodsReceiptBuilder {
    private final InboundDelivery delivery;
    private UserPrincipal manager = UserMother.manager();
    private String gateNumber = "GATE-1";

    public static GoodsReceiptBuilder aReceipt(InboundDelivery delivery) {
        return new GoodsReceiptBuilder(delivery);
    }

    private GoodsReceiptBuilder(InboundDelivery delivery) {
        this.delivery = delivery;
    }

    public GoodsReceiptBuilder withManager(UserPrincipal manager) { this.manager = manager; return this; }
    public GoodsReceiptBuilder withGate(String gate) { this.gateNumber = gate; return this; }

    public GoodsReceipt build() {
        return GoodsReceipt.open(
                manager.id(),
                manager.nickname(),
                delivery.getId(),
                delivery.getWarehouseId(),
                delivery.getReceivingMode(),
                delivery.getAsnNumber(),
                gateNumber
        );
    }
}