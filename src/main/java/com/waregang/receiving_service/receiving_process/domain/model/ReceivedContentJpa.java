package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "received_contents"
//        @UniqueConstraint(name = "uk_unit_sku", columnNames = {"container_unit_id", "sku"})
)
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

    private ReceivedContentJpa(String sku, Long quantity, ReceivedUnitJpa containerUnit) {
        this.id = IdGenerator.generate();
        this.sku = sku;
        this.quantity = quantity;
        this.containerUnit = containerUnit;
    }

    public static ReceivedContentJpa assignToContainer(
            ScanContentRequest scanRequest,
            ReceivedUnitJpa containerUnit
    ) {
        return new ReceivedContentJpa(scanRequest.sku(), scanRequest.quantity(), containerUnit);
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
