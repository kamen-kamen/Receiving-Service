package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.common.idempotency.IdempotencyService;
import com.waregang.receiving_service.integration.IntegrationTestConfig;
import com.waregang.receiving_service.security.api.AuthController;
import com.waregang.receiving_service.security.application.AuthService;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)

@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class AuthExceptionHandlingIT {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @Test
    @DisplayName("Should return Problem Detail for unauthorized")
    void shouldReturn401when() {
        // GIVEN: Настраиваем мок, чтобы он выбрасывал исключение, как будто пароль неверный
        when(authService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // WHEN & THEN
        var body = assertThat(mockMvcTester.post().with(csrf())
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(givenJsonWithBadCredentials()))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson();

        body.extractingPath("$.type").isEqualTo("about:blank");
        body.extractingPath("$.title").isEqualTo("Authentication failed");
        body.extractingPath("$.status").isEqualTo(401);
        body.extractingPath("$.detail").isEqualTo("Bad credentials");
        body.extractingPath("$.instance").isEqualTo("/api/auth/login");
        body.extractingPath("$.timestamp").isNotNull();
    }

    private String givenJsonWithBadCredentials() {
        AuthenticationRequest request = new AuthenticationRequest(
                "existing@gmail.com",
                "wrong_password"
        );
        return jsonMapper.writeValueAsString(request);
    }
}