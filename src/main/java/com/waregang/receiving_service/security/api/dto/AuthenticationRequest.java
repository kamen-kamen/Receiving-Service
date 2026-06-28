package com.waregang.receiving_service.security.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}
