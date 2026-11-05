package com.pranit.todo.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String username,
        boolean enabled,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
