package com.pranit.todo.controller;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.service.TaskService;
import com.pranit.todo.wrapper.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/todos/{id}/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @PostMapping(version = "v1")
    public ResponseEntity<TaskResponse> createTask
            (@PathVariable("id") UUID todoId, @RequestBody @Valid CreateTaskRequest request) {
        final TaskResponse response = this.taskService.createTask(todoId, request);
        return ResponseEntity.created(URI.create("/api/todos/" + todoId + "/tasks/" + response.id())).body(response);
    }

    @GetMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<TaskResponse> getTask(@PathVariable("id") UUID todoId, @PathVariable UUID taskId) {
        final TaskResponse response = this.taskService.getTask(todoId, taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @PathVariable UUID todoId,
            @PageableDefault(sort = "createdAt") Pageable pageable) {
        final PageResponse<TaskResponse> response = this.taskService.getTasks(todoId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<TaskResponse> updateTask
            (@PathVariable("id") UUID todoId, @PathVariable UUID taskId, @RequestBody @Valid UpdateTaskRequest request) {
        final TaskResponse response = this.taskService.updateTask(todoId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{taskId}", version = "v1")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") UUID todoId, @PathVariable UUID taskId) {
        this.taskService.deleteTask(todoId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{taskId}/toggle", version = "v1")
    public ResponseEntity<TaskResponse> toggleTaskCompletion(@PathVariable("id") UUID todoId, @PathVariable UUID taskId) {
        final TaskResponse response = this.taskService.toggleTaskCompletion(todoId, taskId);
        return ResponseEntity.ok(response);
    }
}