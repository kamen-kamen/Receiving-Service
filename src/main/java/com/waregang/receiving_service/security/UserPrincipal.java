package com.waregang.receiving_service.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public record UserPrincipal(
        UUID id,
        String nickname,
        String username,
        String warehouseId,
        List<SimpleGrantedAuthority> authorities
) {}
