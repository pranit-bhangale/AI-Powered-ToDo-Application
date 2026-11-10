package com.pranit.todo.tools;

import com.pranit.todo.dto.CreateTodoRequest;
import com.pranit.todo.dto.TodoResponse;
import com.pranit.todo.dto.UpdateTodoRequest;
import com.pranit.todo.service.TodoService;
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
public class TodoTools {

    private final TodoService todoService;

    @Tool(
            name = "create_todo",
            description = """
                    Creates a new Todo item with optional description, start date, end date, and initial tasks.
                    Start date defaults to today if not provided and must be today or in the future."""
    )
    public TodoResponse createTodo(
            @ToolParam(description = "Todo creation payload") final CreateTodoRequest request
    ) {
        return this.todoService.createTodo(request);
    }

    @Tool(
            name = "get_todo",
            description = "Retrieves a specific Todo by its UUID, including all of its sub-tasks and completion stats."
    )
    public TodoResponse getTodo(
            @ToolParam(description = "UUID of the todo to retrieve") final UUID todoId
    ) {
        return this.todoService.getTodo(todoId);
    }

    @Tool(
            name = "list_todos",
            description = "Retrieves a paginated list of Todos for the authenticated user, with optional page number, size, sort property, and sort direction."
    )
    public PageResponse<TodoResponse> listTodos(
            @ToolParam(description = "Page number to retrieve, 0-indexed (optional, default 0)", required = false) final Integer page,
            @ToolParam(description = "Number of items per page between 1 and 100 (optional, default 10)", required = false) final Integer size,
            @ToolParam(description = "Property to sort by (optional, one of 'createdAt', 'updatedAt', 'title', 'completed', 'startDate', 'endDate'; default 'createdAt')", required = false) final String sortBy,
            @ToolParam(description = "Sort direction (optional, 'ASC' or 'DESC'; default 'DESC')", required = false) final String direction
    ) {
        final Pageable pageable = createPageable(page, size, sortBy, direction);
        return this.todoService.getTodos(pageable);
    }

    private Pageable createPageable(final Integer page, final Integer size, final String sortBy, final String direction) {
        final int pageNum = (page != null && page >= 0) ? page : 0;
        final int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 10;
        final String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : "createdAt";
        final Sort.Direction dir = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageNum, pageSize, Sort.by(dir, sortField));
    }

    @Tool(
            name = "update_todo",
            description = """
                    Updates an existing Todo's title, description, completion status, or end date.
                    Start date is immutable. Todo cannot be marked completed if any sub-tasks are incomplete.
                    """
    )
    public TodoResponse updateTodo(
            @ToolParam(description = "UUID of the todo to update") final UUID todoId,
            @ToolParam(description = "Todo update payload") final UpdateTodoRequest request
    ) {
        return this.todoService.updateTodo(todoId, request);
    }

    @Tool(
            name = "toggle_todo_completion",
            description = "Toggles the completion status of a Todo. If marking complete, all associated sub-tasks must already be completed."
    )
    public TodoResponse toggleTodoCompletion(
            @ToolParam(description = "UUID of the todo whose completion status to toggle") final UUID todoId
    ) {
        return this.todoService.toggleTodoCompletion(todoId);
    }

    @Tool(
            name = "delete_todo",
            description = "Deletes a Todo and cascades deletion to all associated sub-tasks. Fails if any sub-task remains incomplete."
    )
    public String deleteTodo(
            @ToolParam(description = "UUID of the todo to delete") final UUID todoId
    ) {
        this.todoService.deleteTodo(todoId);
        return "Todo with ID " + todoId + " and all associated sub-tasks were successfully deleted.";
    }
}
