package com.pranit.todo.service;

import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.entity.Task;
import com.pranit.todo.entity.Todo;
import com.pranit.todo.entity.User;
import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.model.UserDetail;
import com.pranit.todo.repository.TaskRepository;
import com.pranit.todo.repository.TodoRepository;
import com.pranit.todo.repository.UserRepository;
import com.pranit.todo.service.impl.TodoServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

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
    @DisplayName("toggleTodoCompletion fails when Todo has incomplete tasks")
    void toggleTodoCompletion_failsWhenIncompleteTasksExist() {
        final UUID todoId = UUID.randomUUID();
        final Task incompleteTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .completed(false)
                .build();

        final Todo todo = Todo.builder()
                .id(todoId)
                .title("My Todo")
                .completed(false)
                .tasks(new ArrayList<>(List.of(incompleteTask)))
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserIdWithTasks(todoId, userId)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.toggleTodoCompletion(todoId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("all tasks in this todo must be completed first");
    }

    @Test
    @DisplayName("toggleTodoCompletion succeeds when all tasks are completed")
    void toggleTodoCompletion_succeedsWhenAllTasksCompleted() {
        final UUID todoId = UUID.randomUUID();
        final Task completedTask1 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .completed(true)
                .build();
        final Task completedTask2 = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 2")
                .completed(true)
                .build();

        final Todo todo = Todo.builder()
                .id(todoId)
                .title("My Todo")
                .completed(false)
                .tasks(new ArrayList<>(List.of(completedTask1, completedTask2)))
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserIdWithTasks(todoId, userId)).thenReturn(Optional.of(todo));
        when(taskRepository.existsByTodoIdAndCompletedFalse(todoId)).thenReturn(false);

        final TodoResponse response = todoService.toggleTodoCompletion(todoId);
        assertThat(response.completed()).isTrue();
        assertThat(todo.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("toggleTodoCompletion succeeds when Todo has no tasks")
    void toggleTodoCompletion_succeedsWhenNoTasks() {
        final UUID todoId = UUID.randomUUID();
        final Todo todo = Todo.builder()
                .id(todoId)
                .title("Empty Todo")
                .completed(false)
                .tasks(new ArrayList<>())
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserIdWithTasks(todoId, userId)).thenReturn(Optional.of(todo));
        when(taskRepository.existsByTodoIdAndCompletedFalse(todoId)).thenReturn(false);

        final TodoResponse response = todoService.toggleTodoCompletion(todoId);
        assertThat(response.completed()).isTrue();
    }

    @Test
    @DisplayName("updateTodo with completed=true fails if tasks are incomplete")
    void updateTodo_completedTrueFailsWhenIncompleteTasks() {
        final UUID todoId = UUID.randomUUID();
        final Task incompleteTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Task 1")
                .completed(false)
                .build();

        final Todo todo = Todo.builder()
                .id(todoId)
                .title("My Todo")
                .completed(false)
                .tasks(new ArrayList<>(List.of(incompleteTask)))
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserIdWithTasks(todoId, userId)).thenReturn(Optional.of(todo));

        final UpdateTodoRequest request = new UpdateTodoRequest(null, null, true, null, null, null);

        assertThatThrownBy(() -> todoService.updateTodo(todoId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("all tasks in this todo must be completed first");
    }

    @Test
    @DisplayName("deleteTodo fails when associated tasks are incomplete")
    void deleteTodo_failsWhenIncompleteTasksExist() {
        final UUID todoId = UUID.randomUUID();
        final Todo todo = Todo.builder()
                .id(todoId)
                .title("Todo with incomplete task")
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(Optional.of(todo));
        when(taskRepository.existsByTodoIdAndCompletedFalse(todoId)).thenReturn(true);

        assertThatThrownBy(() -> todoService.deleteTodo(todoId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("all associated tasks must be completed before deleting this Todo");

        verify(taskRepository, never()).deleteByTodoId(any());
        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    @DisplayName("deleteTodo deletes Todo and all related tasks when tasks are all completed")
    void deleteTodo_cascadesAllRelatedTasks() {
        final UUID todoId = UUID.randomUUID();
        final Todo todo = Todo.builder()
                .id(todoId)
                .title("Todo to delete")
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(Optional.of(todo));
        when(taskRepository.existsByTodoIdAndCompletedFalse(todoId)).thenReturn(false);

        todoService.deleteTodo(todoId);

        verify(taskRepository).deleteByTodoId(todoId);
        verify(todoRepository).delete(todo);
    }

    @Test
    @DisplayName("createTodo with past startDate fails")
    void createTodo_pastStartDateFails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        final LocalDate pastDate = LocalDate.now().minusDays(1);
        final CreateTodoRequest request = new CreateTodoRequest("Test Todo", "Desc", pastDate, null, null);

        assertThatThrownBy(() -> todoService.createTodo(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start date cannot be in the past");

        verify(todoRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTodo with current or future startDate succeeds")
    void createTodo_currentOrFutureStartDateSucceeds() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(todoRepository.existsByUserIdAndTitle(eq(userId), any())).thenReturn(false);

        final LocalDate futureDate = LocalDate.now().plusDays(2);
        final CreateTodoRequest request = new CreateTodoRequest("Future Todo", "Desc", futureDate, null, null);

        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final TodoResponse response = todoService.createTodo(request);
        assertThat(response.startDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("updateTodo fails when attempting to change startDate")
    void updateTodo_updatingStartDateFails() {
        final UUID todoId = UUID.randomUUID();
        final LocalDate originalStartDate = LocalDate.now();
        final Todo todo = Todo.builder()
                .id(todoId)
                .title("My Todo")
                .startDate(originalStartDate)
                .user(mockUser)
                .build();

        when(todoRepository.findByIdAndUserIdWithTasks(todoId, userId)).thenReturn(Optional.of(todo));

        final LocalDate newStartDate = originalStartDate.plusDays(5);
        final UpdateTodoRequest request = new UpdateTodoRequest(null, null, null, newStartDate, null, null);

        assertThatThrownBy(() -> todoService.updateTodo(todoId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start date is immutable and cannot be updated");
    }

    @Test
    @DisplayName("getTodos supports user-configured page size")
    void getTodos_supportsUserConfiguredPageSize() {
        final int customPageSize = 25;
        final Pageable pageable = PageRequest.of(0, customPageSize);

        final Todo todo = Todo.builder()
                .id(UUID.randomUUID())
                .title("Todo")
                .user(mockUser)
                .build();

        when(todoRepository.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(todo), pageable, 1));

        final PageResponse<TodoResponse> response = todoService.getTodos(pageable);

        assertThat(response.pageSize()).isEqualTo(customPageSize);
        assertThat(response.contents()).hasSize(1);
        assertThat(response.isFirstPage()).isTrue();
        assertThat(response.isLastPage()).isTrue();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
    }

    @Test
    @DisplayName("getTodos fails with invalid page size > 100 or < 1")
    void getTodos_failsWithInvalidPageSize() {
        final Pageable invalidPageable = PageRequest.of(0, 500);

        assertThatThrownBy(() -> todoService.getTodos(invalidPageable))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Page size must be between 1 and 100");
    }
}
