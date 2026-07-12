package com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities;

import com.waregang.receiving_service.common.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "contents")
public class ContentJpa implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "container_unit_id", nullable = false)
    private HandlingUnitJpa containerUnit;

    public ContentJpa(String sku, int quantity, HandlingUnitJpa containerUnit) {
        this.id = IdGenerator.generate();
        this.sku = sku;
        this.quantity = quantity;
        this.containerUnit = containerUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentJpa other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}