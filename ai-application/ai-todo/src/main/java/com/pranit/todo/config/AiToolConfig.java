package com.pranit.todo.config;

import com.pranit.todo.tools.TaskTools;
import com.pranit.todo.tools.TodoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration for registering Todo and Task AI tools with {@link ChatClient}.
 */
@Configuration
public class AiToolConfig {

    public static final String DEFAULT_SYSTEM_PROMPT = """
            You are an intelligent AI Todo and Task Assistant.
            You help users organize, create, track, complete, and manage their Todos and sub-tasks.
            Always use the provided tools to inspect or mutate state.

            Business Rules & Constraints to remember:
            1. Start dates must be today or in the future (ISO format YYYY-MM-DD). Start date defaults to today if not provided.
            2. Start dates are strictly immutable once created and cannot be updated.
            3. End dates must be on or after the start date.
            4. A Todo cannot be completed until ALL its sub-tasks are completed.
            5. Toggling or marking a sub-task as incomplete automatically reopens a completed parent Todo.
            6. A Todo cannot be deleted until ALL its associated sub-tasks are completed.
            7. When a Todo is deleted, all its completed sub-tasks are automatically cascade deleted.
            """;

    @Bean
    @ConditionalOnBean(ChatClient.Builder.class)
    public ChatClient todoChatClient(
            final ChatClient.Builder chatClientBuilder,
            final TodoTools todoTools,
            final TaskTools taskTools
    ) {
        return chatClientBuilder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultTools(todoTools, taskTools)
                .build();
    }
}
