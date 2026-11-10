package com.pranit.todo.config;

import com.pranit.todo.properties.GuardrailProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring AI configuration for registering Todo and Task AI tools with {@link ChatClient}.
 */
@Configuration
@EnableConfigurationProperties(GuardrailProperties.class)
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

    @Bean("chatClient")
    public ChatClient chatClient(ChatClient.Builder builder, List<Advisor> advisors) {
        return builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(advisors)
                .build();
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor(GuardrailProperties properties) {
        return new SafeGuardAdvisor(properties.blockedWords());
    }

    @Bean
    public List<Advisor> advisors(SafeGuardAdvisor safeGuardAdvisor, MessageChatMemoryAdvisor memoryAdvisor) {
        return List.of(safeGuardAdvisor, memoryAdvisor);
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(2)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor memoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
