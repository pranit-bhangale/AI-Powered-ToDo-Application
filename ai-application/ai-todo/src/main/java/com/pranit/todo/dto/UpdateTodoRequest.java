package com.pranit.todo.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTodoRequest(

        @Size(min = 1, max = 100)
        String title,

        @Size(max = 200)
        String description,

        List<UpdateTaskRequest> tasks

) {
}
