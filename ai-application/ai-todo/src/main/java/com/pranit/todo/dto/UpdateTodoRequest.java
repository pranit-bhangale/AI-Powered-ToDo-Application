package com.pranit.todo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateTodoRequest(
        @Size(min = 1, max = 100, message = "Todo title must be between 1 and 100 characters")
        String title,
        @Size(max = 200, message = "Todo description cannot exceed 200 characters")
        String description,
        Boolean completed,
        LocalDate startDate,
        LocalDate endDate,
        @Valid
        List<UpdateTaskRequest> tasks
) {
}
