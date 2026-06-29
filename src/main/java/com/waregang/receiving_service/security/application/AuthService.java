package com.waregang.receiving_service.security.application;

import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserRepository;
import com.waregang.receiving_service.security.api.dto.AuthenticationRequest;
import com.waregang.receiving_service.security.api.dto.AuthenticationResponse;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor

@Service
@Validated
public class AuthService {
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication auth = authManager.authenticate( new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        ));

        User user = (User) auth.getPrincipal();
        var token = jwtService.generateToken(user);

        return new AuthenticationResponse(token);
    }

    @Transactional
    public void registerBoxCat(RegisterUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createBoxCat(request, encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public void registerBoxManager(RegisterUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createBoxManager(request, encodedPassword);
        userRepository.save(user);
    }
}