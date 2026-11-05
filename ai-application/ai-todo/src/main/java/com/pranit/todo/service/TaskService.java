package com.pranit.todo.service;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.wrapper.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(UUID todoId, CreateTaskRequest request);

    TaskResponse getTask(UUID todoId, UUID taskId);

    PageResponse<TaskResponse> getTasks(UUID todoId, Pageable pageable);

    TaskResponse updateTask(UUID todoId, UUID taskId, UpdateTaskRequest request);

    void deleteTask(UUID todoId, UUID taskId);

    TaskResponse toggleTaskCompletion(UUID todoId, UUID taskId);
}