package com.waregang.receiving_service.fixtures.delivery;

import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;

public class DeliveryMother {
    
    public static InboundDelivery simple() {
        return InboundDeliveryBuilder.aDelivery().build();
    }

    public static InboundDelivery withNestedTree() {
        InboundDelivery delivery = InboundDeliveryBuilder.aDelivery()
                .withAsn("ASN-TREE-01")
                .build();

        HandlingUnit pallet = HandlingUnit.create("PALLET-01", delivery);
        HandlingUnit box = HandlingUnit.create("BOX-01", delivery);
        
        box.fillWithContent("SKU-123", 100);
        pallet.addChild(box);
        delivery.addHandlingUnit(pallet);
        
        return delivery;
    }
}
