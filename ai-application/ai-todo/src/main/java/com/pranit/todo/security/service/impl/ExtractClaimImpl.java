package com.pranit.todo.security.service.impl;

import com.pranit.todo.properties.KeysPathProperties;
import com.pranit.todo.security.load.LoadKey;
import com.pranit.todo.security.service.ExtractClaim;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.UUID;

@Slf4j
@Service
public class ExtractClaimImpl implements ExtractClaim {

    private final PublicKey publicKey;

    public ExtractClaimImpl(KeysPathProperties keysPathProperties) {
        this.publicKey = LoadKey.loadPublicKey(keysPathProperties.publicKeyPath());
    }

    @Override
    public String getUsernameFromAccessToken(final Claims claims) {
        return claims.getSubject();
    }


    @Override
    public UUID getUserIdFromAccessToken(final Claims claims) {
        final String userId = claims.getId();
        return UUID.fromString(userId);
    }

    @Override
    public Claims validateAndParseToken(final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            throw new JwtException("Invalid or expired JWT", ex);
        }
    }

    @Override
    public boolean isAccessToken(final Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }
}
