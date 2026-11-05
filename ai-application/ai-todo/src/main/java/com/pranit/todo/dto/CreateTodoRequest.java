package com.pranit.todo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTodoRequest(
        @NotBlank(message = "Todo title is required")
        @Size(max = 100)
        String title,

        @Size(max = 200)
        String description,

        @Valid
        List<CreateTaskRequest> tasks
) {

    public CreateTodoRequest {
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
    }
}
