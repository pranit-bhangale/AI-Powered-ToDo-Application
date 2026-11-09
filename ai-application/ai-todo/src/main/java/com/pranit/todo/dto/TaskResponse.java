package com.pranit.todo.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record TaskResponse(
        UUID id,
        UUID todoId,
        String title,
        String description,
        boolean completed,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {
}
