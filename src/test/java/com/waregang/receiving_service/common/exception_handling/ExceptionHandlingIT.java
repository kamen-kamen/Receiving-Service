package com.waregang.receiving_service.common.exception_handling;


import com.waregang.receiving_service.common.idempotency.IdempotencyService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
@EnableConfigurationProperties(JwtProperties.class)

@WebMvcTest(controllers = GoodsReceiptController.class)
public class ExceptionHandlingIT{
    @MockitoBean
    private ReceivingProcessService receivingProcessService;

    @MockitoBean
    private GoodsReceiptService receiptService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    JsonMapper jsonMapper;
    
    private static final String URL = "/api/goods-receipts/open";

    @Test
    @DisplayName("Should return 400 with validation error")
    @WithMockUser(authorities = "BOX_MANAGER")
    void shouldReturn400ForInvalidBody() {
        String json = givenStartReceivingJsonWithInvalidParams();

        var body = assertThat(mockMvcTester.post().with(csrf())
                    .uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson();

        body.extractingPath("$.type").isEqualTo("about:blank");
        body.extractingPath("$.title").isEqualTo("Bad request");
        body.extractingPath("$.status").isEqualTo(400);
        body.extractingPath("$.detail").isEqualTo("Validation Error");
        body.extractingPath("$.instance").isEqualTo(URL);

        body.extractingPath("$.invalid_params").asMap().hasSize(2);
        body.extractingPath("$.invalid_params.asnNumber").isEqualTo("must not be blank");
        body.extractingPath("$.timestamp").isNotNull();
    }

    private String givenStartReceivingJsonWithInvalidParams() {
        StartReceivingRequest request = new StartReceivingRequest(
                null,
                "");
        return jsonMapper.writeValueAsString(request);
    }
}
