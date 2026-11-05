package com.pranit.todo.dto;

import lombok.Builder;

import java.util.Collection;
import java.util.UUID;

@Builder
public record AuthenticatedUser(
        UUID id,
        String username,
        String email,
        boolean enabled,
        Collection<String> roles
) {
}
