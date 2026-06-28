package com.waregang.receiving_service.fixtures.user;

import com.waregang.receiving_service.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public class UserBuilder {
    private UUID id = UUID.randomUUID();
    private String nickname = "test_user";
    private String email = "test@wh.com";
    private String warehouseId = "WH-001";
    private String authority = "BOX_CAT";

    public static UserBuilder aUser() { return new UserBuilder(); }

    public UserBuilder withId(UUID id) { this.id = id; return this; }
    public UserBuilder withNickname(String nickname) { this.nickname = nickname; return this; }
    public UserBuilder withWarehouseId(String warehouseId) { this.warehouseId = warehouseId; return this; }
    public UserBuilder withAuthority(String authority) { this.authority = authority; return this; }

    public UserPrincipal build() {
        return new UserPrincipal(
                id, nickname, email, warehouseId,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
