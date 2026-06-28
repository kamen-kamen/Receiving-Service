package com.waregang.receiving_service.security;

import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j

@Profile("dev")
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("manager@warehouse.com").isEmpty()) {
            var request = new RegisterUserRequest(
                    "boss_manager",
                    "WH-CENTRAL",
                    "manager@warehouse.com",
                    "password123"
            );

            User manager = User.createBoxCat(request, passwordEncoder.encode(request.password()));

            manager.setAuthority(Authority.BOX_MANAGER);

            userRepository.save(manager);
            log.info(">>>> Created default BOX_MANAGER user: manager@warehouse.com <<<<");
        }
    }
}
