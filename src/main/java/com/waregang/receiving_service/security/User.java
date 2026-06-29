package com.waregang.receiving_service.security;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.security.api.dto.RegisterUserRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id @Getter
    @Column(name = "id", nullable = false, updatable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false) @Getter
    private String nickname;

    @Column(name = "email", nullable = false, updatable = false, unique = true)
    private String email;

    @Setter // !? is it okay
    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false)
    private Authority authority;

    @Column(name = "warehouseId", nullable = false) @Getter
    private String warehouseId;

    private User(RegisterUserRequest request,
                 String encodedPassword,
                 Authority authority
    ) {
        this.id = IdGenerator.generate();
        this.nickname = request.nickname();
        this.password = encodedPassword;
        this.email = request.email();
        this.authority = authority;
        this.warehouseId = request.warehouseId();
    }

    public static User createBoxCat(RegisterUserRequest request, String encodedPassword) {
        return new User(
                request,
                encodedPassword,
                Authority.BOX_CAT
        );
    }

    public static User createBoxManager(RegisterUserRequest request, String encodedPassword) {
        return new User(
                request,
                encodedPassword,
                Authority.BOX_MANAGER
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(authority.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

//    @Override
//    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
//    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return this.id.equals(other.id);
    }
}