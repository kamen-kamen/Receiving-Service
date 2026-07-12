package com.waregang.receiving_service.integration;

import com.waregang.receiving_service.integration.application.PutAwayMapper;
import com.waregang.receiving_service.integration.infrastrusture.dto.ForwardPutAwayRequest;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnitJpa;
import com.waregang.receiving_service.receiving_process.domain.dto.ReceivedUnitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PutAwayMapperTest {

    private PutAwayMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PutAwayMapper();
    }

    @Test
    void shouldMapToPutAwayRequestDtoCorrectly() {
        // Arrange
        UUID workerSessionId = UUID.randomUUID();
        String occurredOn = "2026-05-23T22:47:00Z";

        ReceivedContentJpa content = mock(ReceivedContentJpa.class);
        when(content.getSku()).thenReturn("SKU-1");
        when(content.getQuantity()).thenReturn(10L);

        ReceivedUnitJpa childUnit = mock(ReceivedUnitJpa.class);
        when(childUnit.getLpn()).thenReturn("CHILD-LPN");
        when(childUnit.getChildUnits()).thenReturn(Collections.emptySet());
        when(childUnit.getContents()).thenReturn(Collections.singleton(content));

        ReceivedUnitJpa rootUnit = mock(ReceivedUnitJpa.class);
        when(rootUnit.getLpn()).thenReturn("ROOT-LPN");
        when(rootUnit.getChildUnits()).thenReturn(Collections.singleton(childUnit));
        when(rootUnit.getContents()).thenReturn(Collections.emptySet());

        List<ReceivedUnitJpa> rootUnits = List.of(rootUnit);

        // Act
        ForwardPutAwayRequest result = mapper.toPutAwayRequestDto(rootUnits, workerSessionId, occurredOn);

        // Assert
        assertThat(result.workerSessionId()).isEqualTo(workerSessionId);
        assertThat(result.timestamp()).isEqualTo(occurredOn);
        assertThat(result.receivedUnits()).hasSize(1);

        ReceivedUnitDto rootDto = result.receivedUnits().get(0);
        assertThat(rootDto.lpn()).isEqualTo("ROOT-LPN");
        assertThat(rootDto.childUnits()).hasSize(1);
        assertThat(rootDto.contents()).isEmpty();

        ReceivedUnitDto childDto = rootDto.childUnits().get(0);
        assertThat(childDto.lpn()).isEqualTo("CHILD-LPN");
        assertThat(childDto.contents()).hasSize(1);
        assertThat(childDto.contents().get(0).containerLpn()).isEqualTo("CHILD-LPN");
        assertThat(childDto.contents().get(0).sku()).isEqualTo("SKU-1");
        assertThat(childDto.contents().get(0).quantity()).isEqualTo(10L);
    }

    @Test
    void shouldReturnEmptyUnitsWhenRootUnitsIsEmpty() {
        // Arrange
        UUID workerSessionId = UUID.randomUUID();
        String occurredOn = "2026-05-23T22:47:00Z";

        // Act
        ForwardPutAwayRequest result = mapper.toPutAwayRequestDto(Collections.emptyList(), workerSessionId, occurredOn);

        // Assert
        assertThat(result.workerSessionId()).isEqualTo(workerSessionId);
        assertThat(result.receivedUnits()).isEmpty();
    }

    @Test
    void shouldMapDeeplyNestedUnitsCorrectly() {
        // Arrange
        UUID workerSessionId = UUID.randomUUID();
        String occurredOn = "2026-05-23T22:47:00Z";

        // Grandchild (with content)
        ReceivedContentJpa content = mock(ReceivedContentJpa.class);
        when(content.getSku()).thenReturn("SKU-1");
        when(content.getQuantity()).thenReturn(5L);

        ReceivedUnitJpa grandChildUnit = mock(ReceivedUnitJpa.class);
        when(grandChildUnit.getLpn()).thenReturn("GRANDCHILD-LPN");
        when(grandChildUnit.getChildUnits()).thenReturn(Collections.emptySet());
        when(grandChildUnit.getContents()).thenReturn(Collections.singleton(content));

        // Child A (with grandchild)
        ReceivedUnitJpa childUnitA = mock(ReceivedUnitJpa.class);
        when(childUnitA.getLpn()).thenReturn("CHILD-A-LPN");
        when(childUnitA.getChildUnits()).thenReturn(Collections.singleton(grandChildUnit));
        when(childUnitA.getContents()).thenReturn(Collections.emptySet());

        // Child B (empty)
        ReceivedUnitJpa childUnitB = mock(ReceivedUnitJpa.class);
        when(childUnitB.getLpn()).thenReturn("CHILD-B-LPN");
        when(childUnitB.getChildUnits()).thenReturn(Collections.emptySet());
        when(childUnitB.getContents()).thenReturn(Collections.emptySet());

        // Root
        ReceivedUnitJpa rootUnit = mock(ReceivedUnitJpa.class);
        when(rootUnit.getLpn()).thenReturn("ROOT-LPN");
        when(rootUnit.getChildUnits()).thenReturn(java.util.Set.of(childUnitA, childUnitB));
        when(rootUnit.getContents()).thenReturn(Collections.emptySet());

        List<ReceivedUnitJpa> rootUnits = List.of(rootUnit);

        // Act
        ForwardPutAwayRequest result = mapper.toPutAwayRequestDto(rootUnits, workerSessionId, occurredOn);

        // Assert
        assertThat(result.receivedUnits()).hasSize(1);
        ReceivedUnitDto rootDto = result.receivedUnits().get(0);
        assertThat(rootDto.childUnits()).hasSize(2);

        // Find child A and B
        ReceivedUnitDto childADto = rootDto.childUnits().stream().filter(u -> u.lpn().equals("CHILD-A-LPN")).findFirst().orElseThrow();
        ReceivedUnitDto childBDto = rootDto.childUnits().stream().filter(u -> u.lpn().equals("CHILD-B-LPN")).findFirst().orElseThrow();

        assertThat(childADto.childUnits()).hasSize(1);
        assertThat(childBDto.childUnits()).isEmpty();

        ReceivedUnitDto grandChildDto = childADto.childUnits().get(0);
        assertThat(grandChildDto.lpn()).isEqualTo("GRANDCHILD-LPN");
        assertThat(grandChildDto.contents()).hasSize(1);
        assertThat(grandChildDto.contents().get(0).sku()).isEqualTo("SKU-1");
    }
}
