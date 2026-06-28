package com.waregang.receiving_service.security.api;

import com.waregang.receiving_service.security.application.AuthService;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.api.dto.AuthenticationResponse;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PreAuthorize("hasAuthority('BOX_MANAGER')")
    @PostMapping("/box-cats")
    public ResponseEntity<?> registerBoxCat(
            @RequestBody RegisterUserRequest request
    ) {
        authService.registerBoxCat(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.authenticate(request));
    }
}
