package com.pranit.todo.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record TodoResponse(
        UUID id,
        String title,
        String description,
        List<TaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt
) {
}