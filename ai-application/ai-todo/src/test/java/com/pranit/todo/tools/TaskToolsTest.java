package com.pranit.todo.tools;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.service.TaskService;
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
class TaskToolsTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskTools taskTools;

    @Test
    @DisplayName("createTask delegates directly to TaskService with UUID and CreateTaskRequest built via Builder")
    void createTask_success() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);

        final CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Write Documentation")
                .description("Include Swagger & Tool annotations")
                .startDate(today)
                .endDate(tomorrow)
                .build();

        final TaskResponse mockResponse = TaskResponse.builder()
                .id(taskId)
                .todoId(todoId)
                .title("Write Documentation")
                .description("Include Swagger & Tool annotations")
                .startDate(today)
                .endDate(tomorrow)
                .completed(false)
                .build();

        when(this.taskService.createTask(todoId, request)).thenReturn(mockResponse);

        final TaskResponse result = this.taskTools.createTask(todoId, request);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.taskService).createTask(todoId, request);
    }

    @Test
    @DisplayName("getTask delegates to TaskService with UUIDs directly")
    void getTask_success() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();
        final TaskResponse mockResponse = TaskResponse.builder().id(taskId).todoId(todoId).title("Task").build();

        when(this.taskService.getTask(todoId, taskId)).thenReturn(mockResponse);

        final TaskResponse result = this.taskTools.getTask(todoId, taskId);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.taskService).getTask(todoId, taskId);
    }

    @Test
    @DisplayName("listTasks creates Pageable and delegates to TaskService with UUID directly")
    void listTasks_success() {
        final UUID todoId = UUID.randomUUID();
        final PageResponse<TaskResponse> mockPage = new PageResponse<>(List.of(), 0, 10, 0L, 0, true, true, false, false);

        when(this.taskService.getTasks(eq(todoId), any(Pageable.class))).thenReturn(mockPage);

        final PageResponse<TaskResponse> result = this.taskTools.listTasks(todoId, 0, 15, "title", "DESC");

        assertThat(result).isEqualTo(mockPage);

        final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(this.taskService).getTasks(eq(todoId), captor.capture());

        final Pageable captured = captor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(0);
        assertThat(captured.getPageSize()).isEqualTo(15);
        assertThat(captured.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("updateTask delegates to TaskService with UUIDs and UpdateTaskRequest built via Builder")
    void updateTask_success() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();
        final LocalDate newEnd = LocalDate.now().plusDays(3);

        final UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Task Title")
                .description("New description")
                .completed(true)
                .endDate(newEnd)
                .build();

        final TaskResponse mockResponse = TaskResponse.builder().id(taskId).completed(true).build();

        when(this.taskService.updateTask(todoId, taskId, request)).thenReturn(mockResponse);

        final TaskResponse result = this.taskTools.updateTask(todoId, taskId, request);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.taskService).updateTask(todoId, taskId, request);
    }

    @Test
    @DisplayName("toggleTaskCompletion delegates to TaskService with UUIDs directly")
    void toggleTaskCompletion_success() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();
        final TaskResponse mockResponse = TaskResponse.builder().id(taskId).completed(true).build();

        when(this.taskService.toggleTaskCompletion(todoId, taskId)).thenReturn(mockResponse);

        final TaskResponse result = this.taskTools.toggleTaskCompletion(todoId, taskId);

        assertThat(result).isEqualTo(mockResponse);
        verify(this.taskService).toggleTaskCompletion(todoId, taskId);
    }

    @Test
    @DisplayName("deleteTask delegates to TaskService with UUIDs directly and returns confirmation")
    void deleteTask_success() {
        final UUID todoId = UUID.randomUUID();
        final UUID taskId = UUID.randomUUID();

        final String result = this.taskTools.deleteTask(todoId, taskId);

        assertThat(result).contains(taskId.toString());
        assertThat(result).contains(todoId.toString());
        verify(this.taskService).deleteTask(todoId, taskId);
    }
}
