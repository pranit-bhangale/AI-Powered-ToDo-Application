package com.pranit.todo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CreateTodoRequest(
        @NotBlank(message = "Todo title is required")
        @Size(max = 100, message = "Todo title cannot exceed 100 characters")
        String title,
        @Size(max = 200, message = "Todo description cannot exceed 200 characters")
        String description,
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,
        LocalDate endDate,
        List<@Valid CreateTaskRequest> tasks
) {

    public CreateTodoRequest {
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }
}
