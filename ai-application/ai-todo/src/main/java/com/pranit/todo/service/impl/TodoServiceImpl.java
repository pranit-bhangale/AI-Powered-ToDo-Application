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
import com.pranit.todo.exception.DuplicateResourceException;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.exception.ResourceNotFoundException;
import com.pranit.todo.page.Page;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.repository.UserRepository;
import com.pranit.todo.security.context.SecurityContext;
import com.pranit.todo.service.TodoService;
import com.pranit.todo.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt", "updatedAt", "title", "completed", "startDate", "endDate"
    );

    private final TodoRepository todoRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse createTodo(final CreateTodoRequest request) {
        final User currentUser = getCurrentUser();

        final String trimmedTitle = request.title().trim();
        if (this.todoRepository.existsByUserIdAndTitle(currentUser.getId(), trimmedTitle)) {
            throw new DuplicateResourceException("Todo with title '" + trimmedTitle + "' already exists.");
        }

        final LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now();
        if (request.startDate() != null && request.startDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Start date cannot be in the past. Please select today or a future date.");
        }
        if (request.endDate() != null && request.endDate().isBefore(startDate)) {
            throw new InvalidRequestException("End date cannot be earlier than start date.");
        }

        Todo todo = Todo.builder()
                .title(trimmedTitle)
                .description(request.description())
                .startDate(startDate)
                .endDate(request.endDate())
                .completed(false)
                .user(currentUser)
                .build();

        if (request.tasks() != null && !request.tasks().isEmpty()) {
            for (final CreateTaskRequest taskRequest : request.tasks()) {
                final LocalDate taskStartDate = taskRequest.startDate() != null ? taskRequest.startDate() : LocalDate.now();
                if (taskRequest.startDate() != null && taskRequest.startDate().isBefore(LocalDate.now())) {
                    throw new InvalidRequestException("Start date for task '" + taskRequest.title() + "' cannot be in the past.");
                }
                if (taskRequest.endDate() != null && taskRequest.endDate().isBefore(taskStartDate)) {
                    throw new InvalidRequestException("End date for task '" + taskRequest.title() + "' cannot be earlier than its start date.");
                }

                final Task task = Task.builder()
                        .title(taskRequest.title().trim())
                        .description(taskRequest.description())
                        .startDate(taskStartDate)
                        .endDate(taskRequest.endDate())
                        .completed(false)
                        .todo(todo)
                        .build();
                todo.addTask(task);
            }
        }

        todo = this.todoRepository.save(todo);
        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public TodoResponse getTodo(final UUID todoId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Todo todo = this.todoRepository.findByIdAndUserIdWithTasks(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));
        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = true)
    public PageResponse<TodoResponse> getTodos(final Pageable pageable) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Pageable safePageable = Page.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);
        final org.springframework.data.domain.Page<Todo> todos = this.todoRepository.findByUserId(userId, safePageable);
        return PageResponse.from(todos, this::toResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse updateTodo(final UUID todoId, final UpdateTodoRequest request) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Todo todo = this.todoRepository.findByIdAndUserIdWithTasks(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        // Start date is strictly immutable once created
        if (request.startDate() != null && !request.startDate().equals(todo.getStartDate())) {
            throw new InvalidRequestException("Start date is immutable and cannot be updated.");
        }

        if (request.endDate() != null) {
            if (request.endDate().isBefore(todo.getStartDate())) {
                throw new InvalidRequestException("End date cannot be earlier than start date.");
            }
            todo.setEndDate(request.endDate());
        }

        if (request.title() != null) {
            final String trimmedTitle = request.title().trim();
            if (!trimmedTitle.equals(todo.getTitle())) {
                if (this.todoRepository.existsByUserIdAndTitle(userId, trimmedTitle)) {
                    throw new DuplicateResourceException("Todo with title '" + trimmedTitle + "' already exists.");
                }
                todo.setTitle(trimmedTitle);
            }
        }

        if (request.description() != null) {
            todo.setDescription(request.description());
        }

        if (request.tasks() != null) {
            syncTasks(todo, request.tasks());
        }

        // Completion status update
        if (request.completed() != null) {
            if (Boolean.TRUE.equals(request.completed())) {
                if (todo.hasIncompleteTasks() || this.taskRepository.existsByTodoIdAndCompletedFalse(todoId)) {
                    throw new InvalidRequestException("Cannot complete Todo: all tasks in this todo must be completed first.");
                }
                todo.complete();
            } else {
                todo.reopen();
            }
        }

        // Double check consistency: if todo is marked completed but has incomplete tasks, fail
        if (todo.isCompleted() && (todo.hasIncompleteTasks() || this.taskRepository.existsByTodoIdAndCompletedFalse(todoId))) {
            throw new InvalidRequestException("Cannot complete Todo: all tasks in this todo must be completed first.");
        }

        return this.toResponse(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteTodo(final UUID todoId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Todo todo = this.todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        if (this.taskRepository.existsByTodoIdAndCompletedFalse(todoId)) {
            throw new InvalidRequestException("Cannot delete Todo: all associated tasks must be completed before deleting this Todo.");
        }

        // Industry standard bulk cascade delete for all related tasks
        this.taskRepository.deleteByTodoId(todoId);
        this.todoRepository.delete(todo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TodoResponse toggleTodoCompletion(final UUID todoId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Todo todo = this.todoRepository.findByIdAndUserIdWithTasks(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        if (todo.isCompleted()) {
            todo.reopen();
        } else {
            if (todo.hasIncompleteTasks() || this.taskRepository.existsByTodoIdAndCompletedFalse(todoId)) {
                throw new InvalidRequestException("Cannot complete Todo: all tasks in this todo must be completed first.");
            }
            todo.complete();
        }

        return this.toResponse(todo);
    }

    private void syncTasks(final Todo todo, final List<UpdateTaskRequest> taskRequests) {
        for (final UpdateTaskRequest taskRequest : taskRequests) {
            if (taskRequest.title() == null || taskRequest.title().isBlank()) {
                continue;
            }
            final String trimmedTitle = taskRequest.title().trim();
            final Task existingTask = this.taskRepository.findByTodoIdAndTitle(todo.getId(), trimmedTitle)
                    .orElse(null);

            if (existingTask != null) {
                updateTaskFields(existingTask, taskRequest);
            } else {
                final LocalDate taskStartDate = taskRequest.startDate() != null ? taskRequest.startDate() : LocalDate.now();
                if (taskRequest.startDate() != null && taskRequest.startDate().isBefore(LocalDate.now())) {
                    throw new InvalidRequestException("Start date for task '" + trimmedTitle + "' cannot be in the past.");
                }
                if (taskRequest.endDate() != null && taskRequest.endDate().isBefore(taskStartDate)) {
                    throw new InvalidRequestException("End date for task '" + trimmedTitle + "' cannot be earlier than its start date.");
                }

                final boolean isTaskCompleted = Boolean.TRUE.equals(taskRequest.completed());
                final Task newTask = Task.builder()
                        .title(trimmedTitle)
                        .description(taskRequest.description())
                        .startDate(taskStartDate)
                        .endDate(taskRequest.endDate())
                        .completed(isTaskCompleted)
                        .todo(todo)
                        .build();
                todo.addTask(newTask);
            }
        }
    }

    private void updateTaskFields(final Task task, final UpdateTaskRequest request) {
        if (request.startDate() != null && !request.startDate().equals(task.getStartDate())) {
            throw new InvalidRequestException("Start date is immutable and cannot be updated for task: " + task.getTitle() + ".");
        }
        if (request.endDate() != null) {
            if (request.endDate().isBefore(task.getStartDate())) {
                throw new InvalidRequestException("End date cannot be earlier than start date for task: " + task.getTitle() + ".");
            }
            task.setEndDate(request.endDate());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.completed() != null) {
            if (request.completed()) {
                task.markCompleted();
            } else {
                task.markIncomplete();
            }
        }
    }

    private User getCurrentUser() {
        return this.userRepository.findByUsername(SecurityContext.getCurrentUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user account not found."));
    }

    private TodoResponse toResponse(final Todo todo) {
        final List<TaskResponse> taskResponses = todo.getTasks() != null
                ? todo.getTasks().stream().map(this::toResponse).toList()
                : List.of();

        final int totalTasks = taskResponses.size();
        final int completedTasks = (int) taskResponses.stream().filter(TaskResponse::completed).count();

        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .tasks(taskResponses)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
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
