package com.pranit.todo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record TokenProperties(
        AccessToken accessToken,
        Token token
) {
    public record AccessToken(long expiration) {
    }

    public record Token(String issuer) {
    }
}
