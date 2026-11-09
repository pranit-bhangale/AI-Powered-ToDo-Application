package com.pranit.todo.security.endpoints.impl;

import com.pranit.todo.security.endpoints.PublicEndpointProvider;
import org.springframework.stereotype.Service;

@Service
public class PublicEndpointProviderImpl implements PublicEndpointProvider {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Override
    public String[] publicEndpoints() {
        return PUBLIC_ENDPOINTS;
    }
}
