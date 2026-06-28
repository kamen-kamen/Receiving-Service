package com.waregang.receiving_service.inbound_delivery;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateContentRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateDeliveryRequest;
import com.waregang.receiving_service.inbound_delivery.api.dto.CreateUnitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@AutoConfigureMockMvc
@WithMockUser
class InboundDeliveryValidationIT extends BaseIT {

    private final static String URL = "/api/inbound_deliveries";

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    JsonMapper jsonMapper;

    @Test
    @DisplayName("Should return 400 when content references a non-existent unit")
    void shouldFailWhenContentReferencesNonExistentUnit() {
        // When
        var body = assertThat(mvc.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenRequestWithContentInNonExistingUnit()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        // Then
        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);
        body.extractingPath("$.invalid_params").isNotNull();
    }

    @Test
    @DisplayName("Should return 400 when a unit references a non-existent parent unit")
    void shouldFailWhenUnitReferencesNonExistentParentUnit() {
        // When
        var body = assertThat(mvc.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenRequestWithNonExistingParentUnit()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        // Then
        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);
        body.extractingPath("$.invalid_params").isNotNull();
    }

    @Test
    @DisplayName("Should return 400 when a leaf unit is empty")
    void shouldFailWhenLeafUnitIsEmpty() {
        // When
        var body = assertThat(mvc.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenRequestWithEmptyLeafUnit()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        // Then
        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);
        body.extractingPath("$.invalid_params.contents")
                .isEqualTo("must not be empty");
    }

    private String givenRequestWithEmptyLeafUnit() {
        var palletUnit = new CreateUnitRequest("PALLET", "LPN-PALLET-1", null);
        var boxUnit = new CreateUnitRequest("BOX", "LPN-BOX-2", "LPN-PALLET-1"); // This is a leaf unit and will be empty

        var request = new CreateDeliveryRequest(
                "EXT-ID-3",
                "ASN-3",
                "WH-3",
                LocalDateTime.now(),
                List.of(palletUnit, boxUnit),
                List.of() // No content for LPN-BOX-2
        );
        return jsonMapper.writeValueAsString(request);
    }

    private String givenRequestWithContentInNonExistingUnit() {
        var unitRequest = new CreateUnitRequest("PALLET", "LPN-001", null);
        var contentRequest = new CreateContentRequest("LPN-999", "SKU-123", 10); // LPN-999 does not exist

        var request = new CreateDeliveryRequest(
                "EXT-ID-1",
                "ASN-1",
                "WH-1",
                LocalDateTime.now(),
                List.of(unitRequest),
                List.of(contentRequest)
        );
            return jsonMapper.writeValueAsString(request);
    }

    private String givenRequestWithNonExistingParentUnit() {
        var childUnit = new CreateUnitRequest("BOX", "LPN-002", "LPN-999"); // LPN-999 does not exist
        var parentUnit = new CreateUnitRequest("PALLET", "LPN-001", null);
        var contentRequest = new CreateContentRequest("LPN-002", "SKU-123", 10);

        var request = new CreateDeliveryRequest(
                "EXT-ID-2",
                "ASN-2",
                "WH-2",
                LocalDateTime.now(),
                List.of(parentUnit, childUnit),
                List.of(contentRequest)
        );
        return jsonMapper.writeValueAsString(request);

    }

    @Test
    @DisplayName("Should return 400 when unitRequests is empty")
    void shouldFailWhenUnitRequestsIsEmpty() {
        var request = new CreateDeliveryRequest(
                "EXT-ID-4",
                "ASN-4",
                "WH-4",
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        var body = assertThat(mvc.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);
        body.extractingPath("$.invalid_params").isNotNull();
    }

}
