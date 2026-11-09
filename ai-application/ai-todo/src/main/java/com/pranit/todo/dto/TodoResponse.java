package com.pranit.todo.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record TodoResponse(
        UUID id,
        String title,
        String description,
        boolean completed,
        LocalDate startDate,
        LocalDate endDate,
        int totalTasks,
        int completedTasks,
        List<TaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt
) {
    public TodoResponse {
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }
}