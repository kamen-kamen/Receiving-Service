package com.waregang.receiving_service.fixtures.delivery;

import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import java.util.UUID;

public class InboundDeliveryBuilder {
    private String externalId = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    private String asnNumber = "ASN-" + UUID.randomUUID().toString().substring(0, 8);
    private String warehouseId = "WH-001";

    public static InboundDeliveryBuilder aDelivery() { return new InboundDeliveryBuilder(); }

    public InboundDeliveryBuilder withAsn(String asn) { this.asnNumber = asn; return this; }
    public InboundDeliveryBuilder withWarehouseId(String whId) { this.warehouseId = whId; return this; }

    public InboundDelivery build() {
        return InboundDelivery.create(externalId, asnNumber, warehouseId);
    }
}
