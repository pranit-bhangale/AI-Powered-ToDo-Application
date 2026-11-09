package com.pranit.todo.controller;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.service.TaskService;
import com.pranit.todo.wrapper.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Tasks", description = "REST APIs for Task lifecycle management")
@RestController
@RequestMapping("/api/todos/{todoId}/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create Task under Todo", description = "Creates a new Task under a specified Todo. Start date must be today or in the future.")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or past start date provided")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @ApiResponse(responseCode = "409", description = "Task with the same title already exists in this Todo")
    @PostMapping(version = "v1")
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @RequestBody @Valid CreateTaskRequest request) {
        final TaskResponse response = this.taskService.createTask(todoId, request);
        return ResponseEntity.created(URI.create("/api/todos/" + todoId + "/tasks/" + response.id())).body(response);
    }

    @Operation(summary = "Get paginated Tasks", description = "Retrieves paginated tasks for a specified Todo. Page size is user-configurable.")
    @ApiResponse(responseCode = "200", description = "Paginated tasks returned")
    @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        final PageResponse<TaskResponse> response = this.taskService.getTasks(todoId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Task by ID", description = "Retrieves a single Task by its ID within a specified Todo.")
    @ApiResponse(responseCode = "200", description = "Task retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Todo or Task not found")
    @GetMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @Parameter(description = "UUID of the Task")
            @PathVariable("taskId") UUID taskId) {
        final TaskResponse response = this.taskService.getTask(todoId, taskId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Task", description = "Updates task title, description, completion status, or end date. Start date cannot be modified.")
    @ApiResponse(responseCode = "200", description = "Task updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or attempt to update start date")
    @ApiResponse(responseCode = "404", description = "Todo or Task not found")
    @ApiResponse(responseCode = "409", description = "Task with the same title already exists in this Todo")
    @PutMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @Parameter(description = "UUID of the Task")
            @PathVariable("taskId") UUID taskId,
            @RequestBody @Valid UpdateTaskRequest request) {
        final TaskResponse response = this.taskService.updateTask(todoId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Task", description = "Deletes a specific Task under a Todo.")
    @ApiResponse(responseCode = "204", description = "Task successfully deleted")
    @ApiResponse(responseCode = "404", description = "Todo or Task not found")
    @DeleteMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @Parameter(description = "UUID of the Task")
            @PathVariable("taskId") UUID taskId) {
        this.taskService.deleteTask(todoId, taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle Task completion", description = "Toggles task completion status. Marking incomplete reopens the parent Todo if completed.")
    @ApiResponse(responseCode = "200", description = "Task completion status toggled")
    @ApiResponse(responseCode = "404", description = "Todo or Task not found")
    @PatchMapping(value = "/{taskId}/toggle", version = "v1")
    public ResponseEntity<TaskResponse> toggleTaskCompletion(
            @Parameter(description = "UUID of the parent Todo")
            @PathVariable("todoId") UUID todoId,
            @Parameter(description = "UUID of the Task")
            @PathVariable("taskId") UUID taskId) {
        final TaskResponse response = this.taskService.toggleTaskCompletion(todoId, taskId);
        return ResponseEntity.ok(response);
    }
}
