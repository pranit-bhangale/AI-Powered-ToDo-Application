package com.pranit.todo.chat.event;

/**
 * A tool lifecycle event observed inside the tool-calling loop.
 * Serialized to the browser as the SSE "tool" event.
 */
public record ToolCallEvent(String id, String name, Phase phase, String responseData) {

    public ToolCallEvent(String id, String name, Phase phase) {
        this(id, name, phase, null);
    }

    public enum Phase {
        STARTED, COMPLETED
    }
}