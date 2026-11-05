package com.pranit.todo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @Size(min = 1, max = 100)
        String title,

        @Size(max = 500)
        String description,

        Boolean completed,

        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        LocalDate endDate

) {
}