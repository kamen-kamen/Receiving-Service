package com.waregang.receiving_service.fixtures.user;

import com.waregang.receiving_service.security.UserPrincipal;

public class UserMother {
    public static UserPrincipal manager() {
        return UserBuilder.aUser()
                .withNickname("Boss")
                .withAuthority("BOX_MANAGER")
                .build();
    }

    public static UserPrincipal worker() {
        return UserBuilder.aUser()
                .withNickname("HardWorker")
                .withAuthority("BOX_CAT")
                .build();
    }
    
    public static UserPrincipal worker(String warehouseId) {
        return UserBuilder.aUser()
                .withNickname("HardWorker")
                .withAuthority("BOX_CAT")
                .withWarehouseId(warehouseId)
                .build();
    }
}
