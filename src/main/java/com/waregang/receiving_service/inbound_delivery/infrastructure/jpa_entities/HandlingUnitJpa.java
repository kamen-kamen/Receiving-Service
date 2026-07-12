package com.waregang.receiving_service.inbound_delivery.infrastructure.jpa_entities;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnitType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "handling_units")
public class HandlingUnitJpa implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lpn", unique = true, nullable = false)
    private String lpn;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private HandlingUnitJpa parentUnit;

    @OneToMany(
            mappedBy = "parentUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<HandlingUnitJpa> childUnits = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HandlingUnitType type;

    @OneToMany(
            mappedBy = "containerUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<ContentJpa> contents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_delivery_id", nullable = false)
    private InboundDeliveryJpa inboundDelivery;

    private HandlingUnitJpa(String lpn, InboundDeliveryJpa inboundDelivery) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.type = HandlingUnitType.DEFAULT;
        this.inboundDelivery = inboundDelivery;
    }

    public static HandlingUnitJpa create(String lpn, InboundDeliveryJpa inboundDelivery) {
        return new HandlingUnitJpa(lpn, inboundDelivery);
    }

    public void addChild(HandlingUnitJpa child) {
        this.childUnits.add(child);
        child.parentUnit = this;
    }

    public void fillWithContent(String sku, int quantity) {
        this.contents.add(new ContentJpa(
                sku,
                quantity,
                this
        ));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlingUnitJpa other)) return false;
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