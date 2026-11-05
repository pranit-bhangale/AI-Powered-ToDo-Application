package com.pranit.todo.controller;

import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.service.TodoService;
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
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Validated
public class TodoController {

    private final TodoService todoService;

    @PostMapping(version = "v1")
    public ResponseEntity<TodoResponse> createTodo(@RequestBody @Valid CreateTodoRequest request) {
        final TodoResponse response = this.todoService.createTodo(request);
        return ResponseEntity.created(URI.create("/api/todos/" + response.id())).body(response);
    }

    @GetMapping(value = "/{id}", version = "v1")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable UUID id) {
        final TodoResponse response = this.todoService.getTodo(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<TodoResponse>> getTodos(
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {
        final PageResponse<TodoResponse> response = this.todoService.getTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", version = "v1")
    public ResponseEntity<TodoResponse> updateTodo(@PathVariable UUID id, @RequestBody @Valid UpdateTodoRequest request) {
        final TodoResponse response = this.todoService.updateTodo(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}", version = "v1")
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID id) {
        this.todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/toggle", version = "v1")
    public ResponseEntity<TodoResponse> toggleTodoCompletion(@PathVariable UUID id) {
        final TodoResponse response = this.todoService.toggleTodoCompletion(id);
        return ResponseEntity.ok(response);
    }
}