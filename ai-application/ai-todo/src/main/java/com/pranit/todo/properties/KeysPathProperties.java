package com.pranit.todo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.keys")
public record KeysPathProperties(
        String privateKeyPath,
        String publicKeyPath
) {
}
