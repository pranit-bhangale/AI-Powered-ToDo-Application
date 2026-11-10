package com.pranit.todo.tools;

import com.pranit.todo.dto.CreateTaskRequest;
import com.pranit.todo.dto.TaskResponse;
import com.pranit.todo.dto.UpdateTaskRequest;
import com.pranit.todo.service.TaskService;
import com.pranit.todo.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskTools {

    private final TaskService taskService;

    @Tool(
            name = "create_task",
            description = """
                    Creates a new sub-task under a specific parent Todo.
                    Start date defaults to today and must be today or in the future.
                    Adding a task automatically reopens a completed parent Todo."""
    )
    public TaskResponse createTask(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "Task creation payload") final CreateTaskRequest request
    ) {
        return this.taskService.createTask(todoId, request);
    }

    @Tool(
            name = "get_task",
            description = "Retrieves a specific sub-task under a parent Todo by their UUIDs."
    )
    public TaskResponse getTask(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "UUID of the task to retrieve") final UUID taskId
    ) {
        return this.taskService.getTask(todoId, taskId);
    }

    @Tool(
            name = "list_tasks",
            description = "Retrieves a paginated list of sub-tasks for a given parent Todo."
    )
    public PageResponse<TaskResponse> listTasks(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "Page number to retrieve, 0-indexed (optional, default 0)", required = false) final Integer page,
            @ToolParam(description = "Number of items per page between 1 and 100 (optional, default 10)", required = false) final Integer size,
            @ToolParam(description = "Property to sort by (optional, one of 'createdAt', 'updatedAt', 'title', 'completed', 'startDate', 'endDate'; default 'createdAt')", required = false) final String sortBy,
            @ToolParam(description = "Sort direction (optional, 'ASC' or 'DESC'; default 'DESC')", required = false) final String direction
    ) {
        final Pageable pageable = createPageable(page, size, sortBy, direction);
        return this.taskService.getTasks(todoId, pageable);
    }

    private Pageable createPageable(final Integer page, final Integer size, final String sortBy, final String direction) {
        final int pageNum = (page != null && page >= 0) ? page : 0;
        final int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 10;
        final String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : "createdAt";
        final Sort.Direction dir = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageNum, pageSize, Sort.by(dir, sortField));
    }

    @Tool(
            name = "update_task",
            description = """
                    Updates an existing sub-task's title, description, completion status, or end date.
                    Start date is immutable. Marking a task incomplete reopens the parent Todo."""
    )
    public TaskResponse updateTask(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "UUID of the task to update") final UUID taskId,
            @ToolParam(description = "Task update payload") final UpdateTaskRequest request
    ) {
        return this.taskService.updateTask(todoId, taskId, request);
    }

    @Tool(
            name = "toggle_task_completion",
            description = "Toggles the completion status of a sub-task. If marked incomplete, automatically reopens the parent Todo."
    )
    public TaskResponse toggleTaskCompletion(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "UUID of the task to toggle") final UUID taskId
    ) {
        return this.taskService.toggleTaskCompletion(todoId, taskId);
    }

    @Tool(
            name = "delete_task",
            description = "Deletes a specific sub-task under a parent Todo."
    )
    public String deleteTask(
            @ToolParam(description = "UUID of the parent todo") final UUID todoId,
            @ToolParam(description = "UUID of the task to delete") final UUID taskId
    ) {
        this.taskService.deleteTask(todoId, taskId);
        return "Task with ID " + taskId + " under Todo " + todoId + " was successfully deleted.";
    }
}
