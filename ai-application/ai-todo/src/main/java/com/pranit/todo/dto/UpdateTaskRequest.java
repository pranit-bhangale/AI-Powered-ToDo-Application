package com.pranit.todo.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(min = 1, max = 100, message = "Task title must be between 1 and 100 characters")
        String title,
        @Size(max = 500, message = "Task description cannot exceed 500 characters")
        String description,
        Boolean completed,
        LocalDate startDate,
        LocalDate endDate
) {
}