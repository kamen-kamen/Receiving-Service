package com.waregang.receiving_service.inbound_delivery.domain.model;

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
public class Content implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "container_unit_id", nullable = false)
    private HandlingUnit containerUnit;

    Content(String sku, int quantity, HandlingUnit containerUnit) {
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
        if (!(o instanceof Content other)) return false;
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
