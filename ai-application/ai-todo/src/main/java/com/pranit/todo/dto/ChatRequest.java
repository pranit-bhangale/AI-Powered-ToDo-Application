package com.pranit.todo.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String conversationId,
        @NotBlank(message = "Prompt required")
        String prompt
) {
}
