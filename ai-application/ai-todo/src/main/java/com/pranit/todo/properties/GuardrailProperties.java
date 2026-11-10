package com.pranit.todo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.ai.advisor.safeguard")
public record GuardrailProperties(
        List<String> blockedWords
) {
    public GuardrailProperties {
        blockedWords = blockedWords == null
                ? List.of() : List.copyOf(blockedWords);
    }
}