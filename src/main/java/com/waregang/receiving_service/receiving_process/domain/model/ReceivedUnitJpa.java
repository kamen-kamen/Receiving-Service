package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "received_handling_units",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receipt_lpn", columnNames = {"receiptId", "lpn"})
        }
)
public class ReceivedUnitJpa implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, unique = true, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lpn", nullable = false)
    private String lpn;

    @Column(name = "receiptId", nullable = false, updatable = false)
    private UUID receiptId;

    @Column(name = "worker_receiving_session_id", nullable = false, updatable = false)
    private UUID workerSessionId;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReceivedUnitJpa parentUnit;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "parentUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final Set<ReceivedUnitJpa> childUnits = new HashSet<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "containerUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final Set<ReceivedContentJpa> contents = new HashSet<>();

    private ReceivedUnitJpa(
            String lpn,
            @Nullable
            ReceivedUnitJpa parentUnit,
            UUID workerSessionId,
            UUID receiptId
    ) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.parentUnit = parentUnit;
        this.workerSessionId = workerSessionId;
        this.receiptId = receiptId;
    }

    public static ReceivedUnitJpa assignToParentUnit(
            ScanHandlingUnitRequest scanRequest,
            WorkerReceivingSession session,
            ReceivedUnitJpa proxyParent
    ) {
        return new ReceivedUnitJpa(
                scanRequest.lpn(),
                proxyParent,
                session.getId(),
                session.getReceiptId()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivedUnitJpa other)) return false;
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
