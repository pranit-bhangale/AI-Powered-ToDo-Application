package com.pranit.todo.service;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.entity.Task;
import com.pranit.todo.entity.Todo;
import com.pranit.todo.entity.User;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.model.UserDetail;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.service.impl.TaskServiceImpl;
import com.pranit.todo.wrapper.PageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        final UserDetail userDetail = UserDetail.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .build();

        final var authentication = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createTask with past startDate fails")
    void createTask_pastStartDateFails() {
        final UUID todoId = UUID.randomUUID();
        final Todo todo = Todo.builder().id(todoId).user(mockUser).build();
        when(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(Optional.of(todo));

        final LocalDate pastDate = LocalDate.now().minusDays(1);
        final CreateTaskRequest request = new CreateTaskRequest("Task Title", "Desc", pastDate, null);

        assertThatThrownBy(() -> taskService.createTask(todoId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    @DisplayName("createTask reopens parent Todo if parent Todo was completed")
    void createTask_reopensParentTodoIfCompleted() {
        final UUID todoId = UUID.randomUUID();
        final Todo completedTodo = Todo.builder()
                .id(todoId)
                .title("Completed Todo")
                .completed(true)
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(Optional.of(completedTodo));
        when(taskRepository.existsByTodoIdAndTitle(eq(todoId), any())).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final CreateTaskRequest request = new CreateTaskRequest("New Task", "Desc", LocalDate.now(), null);
        taskService.createTask(todoId, request);

        assertThat(completedTodo.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("updateTask fails when attempting to modify startDate")
    void updateTask_updatingStartDateFails() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();
        final LocalDate originalStartDate = LocalDate.now();

        final Task task = Task.builder()
                .id(taskId)
                .title("Task")
                .startDate(originalStartDate)
                .build();

        when(taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)).thenReturn(Optional.of(task));

        final LocalDate newStartDate = originalStartDate.plusDays(3);
        final UpdateTaskRequest request = new UpdateTaskRequest(null, null, null, newStartDate, null);

        assertThatThrownBy(() -> taskService.updateTask(todoId, taskId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start date is immutable and cannot be updated");
    }

    @Test
    @DisplayName("toggleTaskCompletion from completed to incomplete reopens parent Todo")
    void toggleTaskCompletion_reopensParentTodoWhenIncomplete() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();

        final Todo completedTodo = Todo.builder()
                .id(todoId)
                .completed(true)
                .user(mockUser)
                .build();

        final Task completedTask = Task.builder()
                .id(taskId)
                .title("Task")
                .completed(true)
                .todo(completedTodo)
                .build();

        when(taskRepository.findByIdAndTodoIdAndUserId(taskId, todoId, userId)).thenReturn(Optional.of(completedTask));

        taskService.toggleTaskCompletion(todoId, taskId);

        assertThat(completedTask.isCompleted()).isFalse();
        assertThat(completedTodo.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("getTasks supports user configurable page size")
    void getTasks_supportsUserConfiguredPageSize() {
        final UUID todoId = UUID.randomUUID();
        final int customPageSize = 20;
        final Pageable pageable = PageRequest.of(0, customPageSize);

        when(todoRepository.existsByIdAndUserId(todoId, userId)).thenReturn(true);

        final Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .build();

        when(taskRepository.findByTodoIdAndUserId(eq(todoId), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));

        final PageResponse<TaskResponse> response = taskService.getTasks(todoId, pageable);

        assertThat(response.pageSize()).isEqualTo(customPageSize);
        assertThat(response.contents()).hasSize(1);
    }
}
