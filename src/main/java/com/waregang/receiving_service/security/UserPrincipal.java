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
) {
    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getWarehouseId(),
                List.of(new SimpleGrantedAuthority(user.getAuthority().name()))
        );
    }
}