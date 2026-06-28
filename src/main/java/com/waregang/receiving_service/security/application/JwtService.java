package com.waregang.receiving_service.security.application;

import com.waregang.receiving_service.security.User;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.security.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority; // Добавлен импорт
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor

@Service
public class JwtService {
    private final JwtProperties jwtProperties;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof User u) {
            claims.put("id", u.getId());
            claims.put("warehouseId", u.getWarehouseId());
            claims.put("nickname", u.getNickname());

            // Кладем в токен список строк, а не объектов - иначе 403 неожиданный
            claims.put("authorities", u.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
        }
        return buildToken(claims, userDetails, jwtProperties.expiration());
    }



    public UserPrincipal extractUserPrincipal(String token) {
        Claims claims = extractAllClaims(token);
        // Note: You might need to handle the case where claims.get("workerSessionId") is a String
        // and needs to be converted to a UUID.
        Object idObject = claims.get("id");
        UUID userId = (idObject instanceof UUID) ? (UUID) idObject : UUID.fromString(idObject.toString());

        return new UserPrincipal(
                userId,
                (String) claims.get("nickname"),
                claims.getSubject(),
                (String) claims.get("warehouseId"),
                extractAuthorities(token)
        );
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<SimpleGrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);

        // Этот код теперь будет работать правильно, так как в authorities лежат строки
        List<?> authorities = claims.get("authorities", List.class);

        return authorities.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
