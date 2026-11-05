package com.pranit.todo.service.impl;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.entity.Task;
import com.pranit.todo.entity.Todo;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.exception.ResourceNotFoundException;
import com.pranit.todo.mapper.TaskMapper;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.service.TaskService;
import com.pranit.todo.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TodoRepository todoRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public TaskResponse createTask(final UUID todoId,final CreateTaskRequest request) {
        final Todo todo = this.todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (this.taskRepository.existsByTodoIdAndTitle(todoId, request.title())) {
            throw new InvalidRequestException("Task with title '" + request.title() + "' already exists in this todo");
        }
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .todo(todo)
                .build();
        task = this.taskRepository.save(task);
        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class,readOnly = true)
    public TaskResponse getTask(final UUID todoId,final UUID taskId) {
        final Task task = this.taskRepository.findByIdAndTodoId(taskId, todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class,readOnly = true)
    public PageResponse<TaskResponse> getTasks(final UUID todoId,final Pageable pageable) {
        if (!this.todoRepository.existsById(todoId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }
        final Page<Task> tasks = this.taskRepository.findByTodoId(todoId, pageable);
        return PageResponse.from(tasks, this::toResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public TaskResponse updateTask(final UUID todoId,final UUID taskId, final UpdateTaskRequest request) {
        final Task task = this.taskRepository.findByIdAndTodoId(taskId, todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (request.title() != null && !request.title().equals(task.getTitle())) {
            if (taskRepository.existsByTodoIdAndTitle(todoId, request.title())) {
                throw new InvalidRequestException("Task with title '" + request.title() + "' already exists in this todo");
            }
            task.setTitle(request.title());
        }
        if (request.description() != null) task.setDescription(request.description());
        if (request.completed() != null) {
            if (request.completed()) {
                task.markCompleted();
            } else {
                task.markIncomplete();
            }
        }
        if (request.startDate() != null) task.setStartDate(request.startDate());
        if (request.endDate() != null) task.setEndDate(request.endDate());
        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void deleteTask(final UUID todoId, final UUID taskId) {
        final Task task = this.taskRepository.findByIdAndTodoId(taskId, todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        this.taskRepository.delete(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public TaskResponse toggleTaskCompletion(final UUID todoId, final UUID taskId) {
        final Task task = this.taskRepository.findByIdAndTodoId(taskId, todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (task.isCompleted()) {
            task.markIncomplete();
        } else {
            task.markCompleted();
        }
        return this.toResponse(task);
    }

    private TaskResponse toResponse(final Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}