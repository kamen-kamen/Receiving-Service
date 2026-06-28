package com.waregang.receiving_service.security.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank
        String nickname,

        @NotBlank
        String warehouseId,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
) {}
