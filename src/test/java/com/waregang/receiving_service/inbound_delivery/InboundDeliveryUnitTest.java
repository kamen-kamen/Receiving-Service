package com.waregang.receiving_service.inbound_delivery;

import com.waregang.receiving_service.inbound_delivery.api.dto.CreateContentRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.inbound_delivery.domain.model.HandlingUnit;
import com.waregang.receiving_service.inbound_delivery.domain.model.InboundDelivery;
import com.waregang.receiving_service.inbound_delivery.application.InboundDeliveryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InboundDeliveryUnitTest {

    private InboundDeliveryMapper inboundDeliveryMapper;

    @BeforeEach
    void setUp() {
        inboundDeliveryMapper = new InboundDeliveryMapper();
    }

    @Test
    @DisplayName("Should correctly build a tree structure from a flat list of units")
    void shouldCorrectlyBuildTreeStructure() {
        // Given
        var request = createNestedDeliveryRequest();

        // When
        InboundDelivery delivery = inboundDeliveryMapper.toEntity(request);

        // Then
        HandlingUnit pallet = findUnitByLpn(delivery, "LPN-PALLET")
                .orElseThrow(() -> new AssertionError("Pallet unit not found"));
        HandlingUnit box1 = findUnitByLpn(delivery, "LPN-BOX-1")
                .orElseThrow(() -> new AssertionError("Box unit not found"));

        // Check parent-child relationships
        assertThat(pallet.getChildUnits()).hasSize(1).contains(box1);
        assertThat(box1.getParentUnit()).isEqualTo(pallet);

        // Check content
        assertThat(box1.getContents()).hasSize(1);
        assertThat(box1.getContents().stream().toList().get(0).getSku()).isEqualTo("SKU-123");
        assertThat(pallet.getContents()).isEmpty();

        // Check root units in the delivery
        Set<HandlingUnit> rootUnits = delivery.getHandlingUnits();
        assertThat(rootUnits).hasSize(1).contains(pallet);
    }

    private Optional<HandlingUnit> findUnitByLpn(InboundDelivery delivery, String lpn) {
        return delivery.getHandlingUnits().stream()
                .flatMap(this::flatten)
                .filter(hu -> lpn.equals(hu.getLpn()))
                .findFirst();
    }

    private Stream<HandlingUnit> flatten(HandlingUnit unit) {
        return Stream.concat(
                Stream.of(unit),
                unit.getChildUnits().stream().flatMap(this::flatten)
        );
    }

    private CreateDeliveryRequest createNestedDeliveryRequest() {
        var palletRequest = new CreateUnitRequest("PALLET", "LPN-PALLET", null);
        var box1Request = new CreateUnitRequest("BOX", "LPN-BOX-1", "LPN-PALLET");

        var contentRequest = new CreateContentRequest("LPN-BOX-1", "SKU-123", 10);

        return new CreateDeliveryRequest(
                "EXT-ID",
                "ASN1",
                "WH1",
                LocalDateTime.now(),
                List.of(palletRequest, box1Request),
                List.of(contentRequest)
        );
    }
}
