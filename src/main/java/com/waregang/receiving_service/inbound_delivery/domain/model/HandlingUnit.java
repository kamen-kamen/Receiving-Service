package com.waregang.receiving_service.inbound_delivery.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
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
public class HandlingUnit implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lpn", unique = true, nullable = false)
    private String lpn;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private HandlingUnit parentUnit;

    @OneToMany(
            mappedBy = "parentUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<HandlingUnit> childUnits = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HandlingUnitType type;

    @OneToMany(
            mappedBy = "containerUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<Content> contents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_delivery_id", nullable = false)
    private InboundDelivery inboundDelivery;

    private HandlingUnit(String lpn, InboundDelivery inboundDelivery) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.type = HandlingUnitType.DEFAULT;
        this.inboundDelivery = inboundDelivery;
    }

    public static HandlingUnit create(String lpn, InboundDelivery inboundDelivery) {
        return new HandlingUnit(lpn, inboundDelivery);
    }

    public void addChild(HandlingUnit child) {
        this.childUnits.add(child);
        child.parentUnit = this;
    }

    public void fillWithContent(String sku, int quantity) {
        this.contents.add(new Content(sku, quantity, this));
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
