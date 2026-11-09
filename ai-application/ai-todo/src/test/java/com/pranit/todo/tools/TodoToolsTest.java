package com.pranit.todo.tools;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.service.TodoService;
import com.pranit.todo.wrapper.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoToolsTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoTools todoTools;

    @Test
    @DisplayName("createTodo delegates directly to TodoService with CreateTodoRequest built via Builder")
    void createTodo_success() {
        final UUID todoId = UUID.randomUUID();
        final LocalDate today = LocalDate.now();
        final LocalDate nextWeek = today.plusDays(7);

        final CreateTodoRequest request = CreateTodoRequest.builder()
                .title("Complete AI Assignment")
                .description("Detailed instructions")
                .startDate(today)
                .endDate(nextWeek)
                .tasks(List.of(
                        CreateTaskRequest.builder().title("Subtask 1").build(),
                        CreateTaskRequest.builder().title("Subtask 2").build()
                ))
                .build();

        final TodoResponse mockResponse = TodoResponse.builder()
                .id(todoId)
                .title("Complete AI Assignment")
                .description("Detailed instructions")
                .startDate(today)
                .endDate(nextWeek)
                .completed(false)
                .build();

        when(this.todoService.createTodo(request)).thenReturn(mockResponse);

        final TodoResponse result = this.todoTools.createTodo(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(todoId);
        assertThat(result.title()).isEqualTo("Complete AI Assignment");
        verify(this.todoService).createTodo(request);
    }

    @Test
    @DisplayName("getTodo delegates to TodoService with UUID directly")
    void getTodo_success() {
        final UUID todoId = UUID.randomUUID();
        final TodoResponse mockResponse = TodoResponse.builder().id(todoId).title("Test").build();

        when(this.todoService.getTodo(todoId)).thenReturn(mockResponse);

        final TodoResponse result = this.todoTools.getTodo(todoId);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.todoService).getTodo(todoId);
    }

    @Test
    @DisplayName("listTodos creates Pageable and delegates to TodoService")
    void listTodos_success() {
        final PageResponse<TodoResponse> mockPage = new PageResponse<>(List.of(), 0, 10, 0L, 0, true, true, false, false);
        when(this.todoService.getTodos(any(Pageable.class))).thenReturn(mockPage);

        final PageResponse<TodoResponse> result = this.todoTools.listTodos(1, 20, "title", "ASC");

        assertThat(result).isEqualTo(mockPage);

        final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(this.todoService).getTodos(captor.capture());

        final Pageable captured = captor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(1);
        assertThat(captured.getPageSize()).isEqualTo(20);
        assertThat(captured.getSort().getOrderFor("title")).isNotNull();
        assertThat(captured.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("updateTodo delegates to TodoService with UUID and UpdateTodoRequest built via Builder")
    void updateTodo_success() {
        final UUID todoId = UUID.randomUUID();
        final LocalDate newEnd = LocalDate.now().plusDays(5);

        final UpdateTodoRequest request = UpdateTodoRequest.builder()
                .title("Updated Title")
                .description("Updated desc")
                .completed(true)
                .endDate(newEnd)
                .build();

        final TodoResponse mockResponse = TodoResponse.builder().id(todoId).title("Updated Title").build();

        when(this.todoService.updateTodo(eq(todoId), eq(request))).thenReturn(mockResponse);

        final TodoResponse result = this.todoTools.updateTodo(todoId, request);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.todoService).updateTodo(todoId, request);
    }

    @Test
    @DisplayName("toggleTodoCompletion delegates to TodoService with UUID directly")
    void toggleTodoCompletion_success() {
        final UUID todoId = UUID.randomUUID();
        final TodoResponse mockResponse = TodoResponse.builder().id(todoId).completed(true).build();

        when(this.todoService.toggleTodoCompletion(todoId)).thenReturn(mockResponse);

        final TodoResponse result = this.todoTools.toggleTodoCompletion(todoId);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.todoService).toggleTodoCompletion(todoId);
    }

    @Test
    @DisplayName("deleteTodo delegates to TodoService with UUID directly and returns confirmation")
    void deleteTodo_success() {
        final UUID todoId = UUID.randomUUID();

        final String result = this.todoTools.deleteTodo(todoId);

        assertThat(result).contains(todoId.toString());
        verify(this.todoService).deleteTodo(todoId);
    }
}
