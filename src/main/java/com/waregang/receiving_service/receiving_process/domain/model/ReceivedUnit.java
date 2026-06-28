package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "received_handling_units",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_receipt_lpn", columnNames = {"receiptId", "lpn"})
        }
)
public class ReceivedUnit implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, unique = true, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lpn", nullable = false)
    private String lpn;

    @Column(name = "receiptId", nullable = false, updatable = false)
    private UUID receiptId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_receiving_session_id", nullable = false, updatable = false)
    private WorkerReceivingSession workerSession;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReceivedUnit parentUnit;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "parentUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final Set<ReceivedUnit> childUnits = new HashSet<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "containerUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private final Set<ReceivedContent> contents = new HashSet<>();

    private ReceivedUnit(String lpn, @Nullable ReceivedUnit parentUnit, WorkerReceivingSession workerSession) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.parentUnit = parentUnit;
        this.workerSession = workerSession;
        this.receiptId = workerSession.getReceiptId(); // Денормализация
    }

    public static ReceivedUnit assignToParentUnit(
            ScanHandlingUnitRequest scanRequest,
            WorkerReceivingSession session,
            ReceivedUnit proxyParent
    ) {
        return new ReceivedUnit(
                scanRequest.lpn(),
                proxyParent,
                session
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivedUnit other)) return false;
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
