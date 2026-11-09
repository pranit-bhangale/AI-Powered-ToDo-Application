package com.pranit.todo.version.service;

import org.jspecify.annotations.NullMarked;
import org.springframework.web.accept.ApiVersionParser;

@NullMarked
public final class VersionParser implements ApiVersionParser<Integer> {

    @Override
    public Integer parseVersion(String version) {
        if (!version.matches("v\\d+")) {
            throw new IllegalArgumentException("Invalid API version format: '" + version + "'. Expected format 'v{number}' (e.g., 'v1').");
        }
        return Integer.parseInt(version.substring(1));
    }
}
