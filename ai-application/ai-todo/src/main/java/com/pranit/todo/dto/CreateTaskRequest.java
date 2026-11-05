package com.pranit.todo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 100)
        String title,

        @Size(max = 500)
        String description,

        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        LocalDate endDate

) {
}