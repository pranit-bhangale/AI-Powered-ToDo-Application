package com.pranit.todo.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}