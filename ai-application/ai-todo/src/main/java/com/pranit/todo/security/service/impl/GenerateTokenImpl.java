package com.pranit.todo.security.service.impl;

import com.pranit.todo.model.UserDetail;
import com.pranit.todo.properties.KeysPathProperties;
import com.pranit.todo.properties.TokenProperties;
import com.pranit.todo.security.load.LoadKey;
import com.pranit.todo.security.service.GenerateToken;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateTokenImpl implements GenerateToken {

    private final TokenProperties tokenProperties;
    private final PrivateKey privateKey;

    public GenerateTokenImpl(TokenProperties tokenProperties, KeysPathProperties keysPathProperties) {
        this.tokenProperties = tokenProperties;
        this.privateKey = LoadKey.loadPrivateKey(keysPathProperties.privateKeyPath());
    }

    @Override
    public String generateAccessToken(final UserDetail userDetail) {
        final UUID userId = userDetail.id();
        final String username = userDetail.username();
        final Instant now = Instant.now();
        final Instant expiry = now.plusSeconds(tokenProperties.accessToken().expiration());
        final Set<String> roles = userDetail.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toUnmodifiableSet());
        return Jwts.builder()
                .id(userId.toString())
                .subject(username)
                .issuer(tokenProperties.token().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("roles", roles)
                .claim("token", "access")
                .signWith(this.privateKey)
                .compact();
    }
}
