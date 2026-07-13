package com.waregang.receiving_service.inbound_delivery.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Content {

    private UUID id;
    private String sku;
    private Integer quantity;
    private UUID containerUnitId;

    public Content(String sku, int quantity, UUID containerUnitId) {
        this.id = IdGenerator.generate();
        this.sku = sku;
        this.quantity = quantity;
        this.containerUnitId = containerUnitId;
    }

    public static Content reconstitute(UUID id, String sku, Integer quantity, UUID containerUnitId) {
        Content content = new Content();
        content.id = id;
        content.sku = sku;
        content.quantity = quantity;
        content.containerUnitId = containerUnitId;
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content other)) return false;
        return this.id != null && this.id.equals(other.id);
    }
}