package com.pranit.todo.service;

import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.wrapper.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TodoService {

    TodoResponse createTodo(CreateTodoRequest request);

    TodoResponse getTodo(UUID todoId);

    PageResponse<TodoResponse> getTodos(Pageable pageable);

    PageResponse<TodoResponse> getTodosByUser(UUID userId, Pageable pageable);

    TodoResponse updateTodo(UUID todoId, UpdateTodoRequest request);

    void deleteTodo(UUID todoId);

    TodoResponse toggleTodoCompletion(UUID todoId);
}