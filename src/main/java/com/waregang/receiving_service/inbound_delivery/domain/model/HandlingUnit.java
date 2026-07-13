package com.waregang.receiving_service.inbound_delivery.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HandlingUnit {

    private UUID id;
    private String lpn;
    @Nullable
    private UUID parentUnitId;
    private final Set<HandlingUnit> childUnits = new HashSet<>();
    private HandlingUnitType type;
    private final Set<Content> contents = new HashSet<>();
    private UUID inboundDeliveryId;

    private HandlingUnit(String lpn, UUID inboundDeliveryId) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.type = HandlingUnitType.DEFAULT;
        this.inboundDeliveryId = inboundDeliveryId;
    }

    public static HandlingUnit create(String lpn, UUID inboundDeliveryId) {
        return new HandlingUnit(lpn, inboundDeliveryId);
    }

    public static HandlingUnit reconstitute(UUID id, String lpn, @Nullable UUID parentUnitId, HandlingUnitType type, UUID inboundDeliveryId) {
        HandlingUnit unit = new HandlingUnit();
        unit.id = id;
        unit.lpn = lpn;
        unit.parentUnitId = parentUnitId;
        unit.type = type;
        unit.inboundDeliveryId = inboundDeliveryId;
        return unit;
    }

    public void addChild(HandlingUnit child) {
        this.childUnits.add(child);
        child.parentUnitId = this.id;
    }

    public void fillWithContent(String sku, int quantity) {
        this.contents.add(new Content(
                sku,
                quantity,
                this.id
        ));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlingUnit other)) return false;
        return this.id != null && this.id.equals(other.id);
    }
}