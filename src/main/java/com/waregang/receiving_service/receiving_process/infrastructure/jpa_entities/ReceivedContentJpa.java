package com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities;

import com.waregang.receiving_service.common.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "received_contents")
public class ReceivedContentJpa implements Persistable<UUID> {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_unit_id")
    private ReceivedUnitJpa containerUnit;

    public ReceivedContentJpa(UUID id, String sku, Long quantity, ReceivedUnitJpa containerUnit) {
        this.id = id;
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
        if (!(o instanceof ReceivedContentJpa other)) return false;
        return Objects.equals(this.id, other.id);
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