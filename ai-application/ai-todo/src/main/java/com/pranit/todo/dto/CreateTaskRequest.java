package com.pranit.todo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 100, message = "Task title cannot exceed 100 characters")
        String title,
        @Size(max = 500, message = "Task description cannot exceed 500 characters")
        String description,
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,
        LocalDate endDate
) {
}