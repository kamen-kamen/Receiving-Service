package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReceivedContent {

    private UUID id;
    private String sku;
    private Long quantity;
    private UUID containerUnitId;

    private ReceivedContent(String sku, Long quantity, UUID containerUnitId) {
        this.id = IdGenerator.generate();
        this.sku = sku;
        this.quantity = quantity;
        this.containerUnitId = containerUnitId;
    }

    public static ReceivedContent create(String sku, Long quantity, UUID containerUnitId) {
        return new ReceivedContent(sku, quantity, containerUnitId);
    }

    public static ReceivedContent reconstitute(UUID id, String sku, Long quantity, UUID containerUnitId) {
        ReceivedContent content = new ReceivedContent();
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
        if (!(o instanceof ReceivedContent other)) return false;
        return Objects.equals(this.id, other.id);
    }
}