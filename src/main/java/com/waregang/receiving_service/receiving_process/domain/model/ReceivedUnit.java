package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReceivedUnit {

    private UUID id;
    private String lpn;
    private UUID receiptId;
    private UUID workerSessionId;
    @Nullable
    private UUID parentUnitId;
    private final Set<ReceivedUnit> childUnits = new HashSet<>();
    private final Set<ReceivedContent> contents = new HashSet<>();

    private ReceivedUnit(
            String lpn,
            @Nullable UUID parentUnitId,
            UUID workerSessionId,
            UUID receiptId
    ) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.parentUnitId = parentUnitId;
        this.workerSessionId = workerSessionId;
        this.receiptId = receiptId;
    }

    // This factory method will be used by the service layer
    public static ReceivedUnit create(
            String lpn,
            @Nullable UUID parentUnitId,
            UUID workerSessionId,
            UUID receiptId
    ) {
        return new ReceivedUnit(lpn, parentUnitId, workerSessionId, receiptId);
    }

    public static ReceivedUnit reconstitute(
            UUID id,
            String lpn,
            @Nullable UUID parentUnitId,
            UUID workerSessionId,
            UUID receiptId
    ) {
        ReceivedUnit unit = new ReceivedUnit();
        unit.id = id;
        unit.lpn = lpn;
        unit.parentUnitId = parentUnitId;
        unit.workerSessionId = workerSessionId;
        unit.receiptId = receiptId;
        return unit;
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
}