package com.annam.shop.security;

import com.annam.shop.config.JwtProperties;
import com.annam.shop.domain.Role;
import com.annam.shop.service.view.Views.CustomerView;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String create(CustomerView customer) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresInSeconds());
        return Jwts.builder()
                .subject(String.valueOf(customer.id()))
                .claim("email", customer.email())
                .claim("role", customer.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public long expiresInSeconds() {
        return properties.getExpirationMinutes() * 60;
    }

    public AuthUser parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return new AuthUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
