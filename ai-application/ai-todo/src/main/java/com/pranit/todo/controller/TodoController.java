package com.pranit.todo.controller;

import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.service.TodoService;
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

@Tag(name = "Todos", description = "Industry-standard REST APIs for Todo lifecycle management")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Validated
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Create Todo", description = "Creates a new Todo item with optional initial tasks. Start date must be today or in the future.")
    @ApiResponse(responseCode = "201", description = "Todo created successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed or invalid dates provided")
    @ApiResponse(responseCode = "409", description = "Todo with the given title already exists")
    @PostMapping(version = "v1")
    public ResponseEntity<TodoResponse> createTodo(
            @RequestBody @Valid CreateTodoRequest request) {
        final TodoResponse response = this.todoService.createTodo(request);
        return ResponseEntity.created(URI.create("/api/todos/" + response.id())).body(response);
    }

    @Operation(summary = "Get paginated Todos", description = "Retrieves a paginated list of Todos for the authenticated user (resolved from security context). Page size is user-configurable.")
    @ApiResponse(responseCode = "200", description = "Paginated Todos returned")
    @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters")
    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<TodoResponse>> getTodos(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        final PageResponse<TodoResponse> response = this.todoService.getTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Todo by ID", description = "Retrieves a single Todo along with its tasks.")
    @ApiResponse(responseCode = "200", description = "Todo retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @GetMapping(value = "/{id}", version = "v1")
    public ResponseEntity<TodoResponse> getTodo(
            @Parameter(description = "UUID of the Todo") @PathVariable("id") UUID id) {
        final TodoResponse response = this.todoService.getTodo(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Todo", description = "Updates title, description, tasks, or completion status. Start date cannot be modified. Todo can only be completed if all tasks are completed.")
    @ApiResponse(responseCode = "200", description = "Todo updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or attempt to modify start date / complete without all tasks done")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @ApiResponse(responseCode = "409", description = "Todo with title already exists")
    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<TodoResponse> updateTodo(
            @Parameter(description = "UUID of the Todo") @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateTodoRequest request) {
        final TodoResponse response = this.todoService.updateTodo(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Todo and cascade tasks", description = "Deletes a Todo and cascades deletion to all related tasks. A Todo cannot be deleted if any associated task is incomplete.")
    @ApiResponse(responseCode = "204", description = "Todo and all associated tasks successfully deleted")
    @ApiResponse(responseCode = "400", description = "Cannot delete Todo because some associated tasks are not completed")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "UUID of the Todo") @PathVariable("id") UUID id) {
        this.todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle Todo completion", description = "Toggles Todo completion status. A Todo can only be completed if all tasks in it are completed.")
    @ApiResponse(responseCode = "200", description = "Todo status toggled successfully")
    @ApiResponse(responseCode = "400", description = "Cannot complete Todo because some tasks remain incomplete")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    @PatchMapping(value = "/{id}/toggle", version = "v1")
    public ResponseEntity<TodoResponse> toggleTodoCompletion(
            @Parameter(description = "UUID of the Todo") @PathVariable("id") UUID id) {
        final TodoResponse response = this.todoService.toggleTodoCompletion(id);
        return ResponseEntity.ok(response);
    }
}
