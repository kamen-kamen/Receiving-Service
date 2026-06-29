package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.BaseIT;
import com.waregang.receiving_service.security.application.AuthService;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@AutoConfigureMockMvc
public class AuthExceptionHandlingIT extends BaseIT {

    @Autowired
    JsonMapper jsonMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    @DisplayName("Should return Problem Detail for unauthorized")
    void shouldReturn401when() {

        authService.registerBoxCat(new RegisterUserRequest(
                "nickname",
                UUID.randomUUID().toString(),
                "existing@gmail.com",
                "password"));

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
