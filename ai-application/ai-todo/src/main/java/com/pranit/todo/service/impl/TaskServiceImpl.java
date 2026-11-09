package com.pranit.todo.service.impl;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.entity.Task;
import com.pranit.todo.entity.Todo;
import com.pranit.todo.exception.DuplicateResourceException;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.exception.ResourceNotFoundException;
import com.pranit.todo.page.Page;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.security.context.SecurityContext;
import com.pranit.todo.service.TaskService;
import com.pranit.todo.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt", "updatedAt", "title", "completed", "startDate", "endDate"
    );

    private final TaskRepository taskRepository;
    private final TodoRepository todoRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TaskResponse createTask(final UUID todoId, final CreateTaskRequest request) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Todo todo = this.todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        final String trimmedTitle = request.title().trim();
        if (this.taskRepository.existsByTodoIdAndTitle(todoId, trimmedTitle)) {
            throw new DuplicateResourceException("Task with title '" + trimmedTitle + "' already exists in this todo.");
        }

        final LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now();
        if (request.startDate() != null && request.startDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Start date cannot be in the past. Please select today or a future date.");
        }
        if (request.endDate() != null && request.endDate().isBefore(startDate)) {
            throw new InvalidRequestException("End date cannot be earlier than start date.");
        }

        Task task = Task.builder()
                .title(trimmedTitle)
                .description(request.description())
                .startDate(startDate)
                .endDate(request.endDate())
                .completed(false)
                .todo(todo)
                .build();

        // Adding an incomplete task means the parent todo can no longer be complete
        if (todo.isCompleted()) {
            todo.reopen();
        }

        task = this.taskRepository.save(task);
        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public TaskResponse getTask(final UUID todoId, final UUID taskId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Task task = this.taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public PageResponse<TaskResponse> getTasks(final UUID todoId, final Pageable pageable) {
        final UUID userId = SecurityContext.getCurrentUserId();
        if (!this.todoRepository.existsByIdAndUserId(todoId, userId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + todoId);
        }

        final Pageable safePageable = Page.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);
        final org.springframework.data.domain.Page<Task> tasks = this.taskRepository.findByTodoIdAndUserId(todoId, userId, safePageable);
        return PageResponse.from(tasks, this::toResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TaskResponse updateTask(final UUID todoId, final UUID taskId, final UpdateTaskRequest request) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Task task = this.taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Start date is strictly immutable once created
        if (request.startDate() != null && !request.startDate().equals(task.getStartDate())) {
            throw new InvalidRequestException("Start date is immutable and cannot be updated.");
        }

        if (request.endDate() != null) {
            if (request.endDate().isBefore(task.getStartDate())) {
                throw new InvalidRequestException("End date cannot be earlier than start date.");
            }
            task.setEndDate(request.endDate());
        }

        if (request.title() != null) {
            final String trimmedTitle = request.title().trim();
            if (!trimmedTitle.equals(task.getTitle())) {
                if (this.taskRepository.existsByTodoIdAndTitle(todoId, trimmedTitle)) {
                    throw new DuplicateResourceException("Task with title '" + trimmedTitle + "' already exists in this todo.");
                }
                task.setTitle(trimmedTitle);
            }
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.completed() != null) {
            if (request.completed()) {
                task.markCompleted();
            } else {
                task.markIncomplete(); // Automatically reopens the parent todo if it was completed
            }
        }

        return this.toResponse(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteTask(final UUID todoId, final UUID taskId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Task task = this.taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        this.taskRepository.delete(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TaskResponse toggleTaskCompletion(final UUID todoId, final UUID taskId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Task task = this.taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (task.isCompleted()) {
            task.markIncomplete(); // Reopens the parent todo if it was completed
        } else {
            task.markCompleted();
        }

        return this.toResponse(task);
    }

    private TaskResponse toResponse(final Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .todoId(task.getTodo() != null ? task.getTodo().getId() : null)
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
