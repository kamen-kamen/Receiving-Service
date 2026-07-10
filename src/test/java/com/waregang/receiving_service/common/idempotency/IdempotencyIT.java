package com.waregang.receiving_service.common.idempotency;

import com.waregang.receiving_service.integration.IntegrationTestConfig;
import com.waregang.receiving_service.receiving_process.api.GoodsReceiptController;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.security.application.AuthService;
import com.waregang.receiving_service.security.configuration.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
@EnableConfigurationProperties(JwtProperties.class)
@WebMvcTest(controllers = GoodsReceiptController.class)
class IdempotencyIT {

    @MockitoBean
    private ReceivingProcessService receivingProcessService;

    @MockitoBean
    private GoodsReceiptService receiptService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private JsonMapper jsonMapper;

    private static final String URL = "/api/goods-receipts/open";

    @Test
    @DisplayName("Should return 409 Conflict for duplicate request with the same idempotency key")
    @WithMockUser(authorities = "BOX_MANAGER")
    void shouldReturnConflictForDuplicateRequest() {
        // Given
        String idempotencyKey = UUID.randomUUID().toString();
        StartReceivingRequest request = new StartReceivingRequest("ASN-123", "GATE-1");
        String json = jsonMapper.writeValueAsString(request);

        // Первый запрос - блокировка успешна
        when(idempotencyService.tryLock(anyString())).thenReturn(true);
        // Мокаем успешный ответ от сервиса получения
        when(receiptService.startReceiving(any(), any())).thenReturn(null);

        // First request should succeed
        assertThat(mockMvcTester.post().with(csrf())
                .uri(URL)
                .header("X-Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED);

        // Второй запрос - блокировка НЕ успешна (конфликт)
        when(idempotencyService.tryLock(anyString())).thenReturn(false);

        // Second request with the same key should fail
        assertThat(mockMvcTester.post().with(csrf())
                .uri(URL)
                .header("X-Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should process requests normally if idempotency key is not provided")
    @WithMockUser(authorities = "BOX_MANAGER")
    void shouldSucceedWithoutIdempotencyKey() {
        // Given
        StartReceivingRequest request = new StartReceivingRequest("ASN-123", "GATE-1");
        String json = jsonMapper.writeValueAsString(request);

        // Мокаем успешный ответ от сервиса получения
        when(receiptService.startReceiving(any(), any())).thenReturn(null);

        // When & Then
        assertThat(mockMvcTester.post().with(csrf())
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED);
    }
}