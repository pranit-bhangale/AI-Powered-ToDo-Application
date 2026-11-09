package com.pranit.todo.security;

import com.pranit.todo.dto.TokenResponse;
import com.pranit.todo.model.UserDetail;
import com.pranit.todo.properties.KeysPathProperties;
import com.pranit.todo.properties.TokenProperties;
import com.pranit.todo.security.load.LoadKey;
import com.pranit.todo.security.service.impl.ExtractClaimImpl;
import com.pranit.todo.security.service.impl.GenerateTokenImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenSecurityTest {

    private GenerateTokenImpl generateToken;
    private ExtractClaimImpl extractClaim;
    private PrivateKey privateKey;

    @BeforeEach
    void setUp() {
        final KeysPathProperties keysPathProperties = new KeysPathProperties(
                "classpath:keys/private.pem",
                "classpath:keys/public.pem"
        );
        final TokenProperties tokenProperties = new TokenProperties(
                new TokenProperties.AccessToken(3600),
                new TokenProperties.Token("ai-todo")
        );

        generateToken = new GenerateTokenImpl(tokenProperties, keysPathProperties);
        extractClaim = new ExtractClaimImpl(keysPathProperties);
        privateKey = LoadKey.loadPrivateKey(keysPathProperties.privateKeyPath());
    }

    @Test
    @DisplayName("Generated access token strictly contains type 'Bearer' and token 'access'")
    void generatedToken_strictlyValidated() {
        final UUID userId = UUID.randomUUID();
        final UserDetail userDetail = UserDetail.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .enabled(true)
                .build();

        final TokenResponse tokenResponse = generateToken.generateAccessToken(userDetail);
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotBlank();
        assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");

        final Claims claims = extractClaim.validateAndParseToken(tokenResponse.accessToken());
        assertThat(claims).isNotNull();
        assertThat(claims.get("type", String.class)).isEqualTo("Bearer");
        assertThat(claims.get("token", String.class)).isEqualTo("access");
        assertThat(extractClaim.isAccessToken(claims)).isTrue();
        assertThat(extractClaim.getUsernameFromAccessToken(claims)).isEqualTo("testuser");
        assertThat(extractClaim.getUserIdFromAccessToken(claims)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Token fails strict validation if type is not Bearer")
    void tokenFails_whenTypeIsNotBearer() {
        final Instant now = Instant.now();
        final String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("testuser")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("type", "Custom")
                .claim("token", "access")
                .signWith(privateKey)
                .compact();

        final Claims claims = extractClaim.validateAndParseToken(token);
        assertThat(extractClaim.isAccessToken(claims)).isFalse();
    }

    @Test
    @DisplayName("Token fails strict validation if token is not access")
    void tokenFails_whenTokenIsNotAccess() {
        final Instant now = Instant.now();
        final String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("testuser")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("type", "Bearer")
                .claim("token", "refresh")
                .signWith(privateKey)
                .compact();

        final Claims claims = extractClaim.validateAndParseToken(token);
        assertThat(extractClaim.isAccessToken(claims)).isFalse();
    }

    @Test
    @DisplayName("Token fails strict validation when using alternative claim names (no fallback)")
    void tokenFails_whenUsingAlternativeClaimNamesWithoutTypeAndToken() {
        final Instant now = Instant.now();
        final String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("testuser")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("token_type", "Bearer")
                .signWith(privateKey)
                .compact();

        final Claims claims = extractClaim.validateAndParseToken(token);
        assertThat(extractClaim.isAccessToken(claims)).isFalse();
    }
}
