package com.pranit.todo.security.service.impl;

import com.pranit.todo.security.service.ResolveAccessToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolveAccessTokenImpl implements ResolveAccessToken {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public String getAccessTokenFromRequest(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.regionMatches(
                true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())
        ) {
            return null;
        }
        final String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }
}
