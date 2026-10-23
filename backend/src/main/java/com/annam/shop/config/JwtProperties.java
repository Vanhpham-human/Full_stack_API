package com.annam.shop.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMinutes = 720;

    @PostConstruct
    void validate() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret phải dài ít nhất 32 byte");
        }
    }
}
