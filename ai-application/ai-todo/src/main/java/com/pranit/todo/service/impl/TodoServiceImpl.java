package com.pranit.todo.service.impl;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.entity.Task;
import com.pranit.todo.entity.Todo;
import com.pranit.todo.entity.User;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.exception.ResourceNotFoundException;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.repository.UserRepository;
import com.pranit.todo.security.context.SecurityContext;
import com.pranit.todo.service.TodoService;
import com.pranit.todo.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse createTodo(final CreateTodoRequest request) {
        final User currentUser = getCurrentUser();
        if (this.todoRepository.existsByUserIdAndTitle(currentUser.getId(), request.title())) {
            throw new InvalidRequestException("Todo with title '" + request.title() + "' already exists");
        }
        Todo todo = Todo.builder()
                .title(request.title())
                .description(request.description())
                .user(currentUser)
                .build();
        if (request.tasks() != null && !request.tasks().isEmpty()) {
            for (CreateTaskRequest taskRequest : request.tasks()) {
                Task task = Task.builder()
                        .title(taskRequest.title())
                        .description(taskRequest.description())
                        .startDate(taskRequest.startDate())
                        .endDate(taskRequest.endDate())
                        .todo(todo)
                        .build();
                todo.addTask(task);
            }
        }
        todo = todoRepository.save(todo);
        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public TodoResponse getTodo(final UUID todoId) {
        final User currentUser = getCurrentUser();
        final Todo todo = this.todoRepository.findByIdWithTasks(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }
        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public PageResponse<TodoResponse> getTodos(final Pageable pageable) {
        final User currentUser = getCurrentUser();
        final Page<Todo> todos = this.todoRepository.findByUserId(currentUser.getId(), pageable);
        return PageResponse.from(todos, this::toResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public PageResponse<TodoResponse> getTodosByUser(final UUID userId, final Pageable pageable) {
        final User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new InvalidRequestException("Cannot access todos of another user");
        }
        final Page<Todo> todos = this.todoRepository.findByUserId(userId, pageable);
        return PageResponse.from(todos, this::toResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse updateTodo(final UUID todoId, final UpdateTodoRequest request) {
        final User currentUser = getCurrentUser();
        Todo todo = this.todoRepository.findByIdWithTasks(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }
        if (request.title() != null && !request.title().equals(todo.getTitle())) {
            if (this.todoRepository.existsByUserIdAndTitle(currentUser.getId(), request.title())) {
                throw new InvalidRequestException("Todo with title '" + request.title() + "' already exists");
            }
            todo.setTitle(request.title());
        }
        if (request.description() != null) todo.setDescription(request.description());
        if (request.tasks() != null) syncTasks(todo, request.tasks());
        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteTodo(final UUID todoId) {
        final User currentUser = getCurrentUser();
        final Todo todo = this.todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }
        this.todoRepository.delete(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse toggleTodoCompletion(final UUID todoId) {
        User currentUser = getCurrentUser();
        final Todo todo = this.todoRepository.findByIdWithTasks(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        if (!todo.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }
        if (todo.isCompleted()) {
            todo.reopen();
        } else {
            todo.complete();
        }
        return this.toResponse(todo);
    }

    private void syncTasks(final Todo todo, final List<UpdateTaskRequest> taskRequests) {
        for (UpdateTaskRequest taskRequest : taskRequests) {
            if (taskRequest.title() != null) {
                final Task existingTask = this.taskRepository.findByTodoIdAndTitle(todo.getId(), taskRequest.title())
                        .orElse(null);
                if (existingTask != null) {
                    updateTaskFields(existingTask, taskRequest);
                } else {
                    final Task newTask = Task.builder()
                            .title(taskRequest.title())
                            .description(taskRequest.description())
                            .startDate(taskRequest.startDate())
                            .endDate(taskRequest.endDate())
                            .todo(todo)
                            .build();
                    todo.addTask(newTask);
                }
            }
        }
    }

    private void updateTaskFields(final Task task, final UpdateTaskRequest request) {
        if (request.title() != null) task.setTitle(request.title());
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
    }

    private User getCurrentUser() {
        return this.userRepository.findByUsername(SecurityContext.getCurrentUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private TodoResponse toResponse(final Todo todo) {
        final List<TaskResponse> taskResponses = todo.getTasks() != null
                ? todo.getTasks().stream().map(this::toResponse).toList() : List.of();
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .tasks(taskResponses)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
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

    private TodoResponse toResponseWithoutTasks(final Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .tasks(List.of())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}