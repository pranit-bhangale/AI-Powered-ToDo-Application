package com.pranit.todo.page;

import com.pranit.todo.exception.InvalidRequestException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides pagination and sort validation utilities for Spring Data.
 */
public final class Page {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    private Page() {
        throw new UnsupportedOperationException("Page is a utility class and cannot be instantiated.");
    }

    /**
     * Validates and sanitizes a Pageable against allowed sort properties and bounds.
     */
    public static Pageable validatePageable(
            final @Nullable Pageable pageable,
            final @Nullable Collection<String> allowedSortProperties
    ) {
        final Map<String, String> allowedProperties = normalizeAllowedSortProperties(allowedSortProperties);
        return validatePageableInternal(pageable, allowedProperties);
    }

    private static Map<String, String> normalizeAllowedSortProperties(final @Nullable Collection<String> allowedSortProperties) {
        if (allowedSortProperties == null || allowedSortProperties.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> normalized = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final String property : allowedSortProperties) {
            Assert.hasText(property, "Allowed sort properties must not be null or blank");
            final String trimmed = property.trim();
            normalized.put(trimmed, trimmed);
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static Pageable validatePageableInternal(final @Nullable Pageable pageable, final Map<String, String> allowedSortProperties) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE);
        }
        validatePageNumber(pageable.getPageNumber());
        validatePageSize(pageable.getPageSize());
        final Sort safeSort = validateSort(pageable.getSort(), allowedSortProperties);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }

    private static void validatePageNumber(final int page) {
        if (page < 0) {
            throw new InvalidRequestException("Page number must be greater than or equal to 0.");
        }
    }

    private static void validatePageSize(final int size) {
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE + ".");
        }
    }

    private static Sort validateSort(final @Nullable Sort sort, final Map<String, String> allowedSortProperties) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.unsorted();
        }
        final List<Sort.Order> orders = sort.stream()
                .map(order -> {
                    final String canonicalProperty = validateSortBy(order.getProperty(), allowedSortProperties);
                    return order.withProperty(canonicalProperty);
                })
                .toList();
        return Sort.by(orders);
    }

    private static String validateSortBy(final String sortBy, final Map<String, String> allowedSortProperties) {
        Assert.hasText(sortBy, "Sort property must not be null or blank");
        final String trimmed = sortBy.trim();
        if (allowedSortProperties.isEmpty()) {
            throw new InvalidRequestException("Sorting is not supported for this query.");
        }
        final String canonical = allowedSortProperties.get(trimmed);
        if (canonical == null) {
            throw new InvalidRequestException("Unsupported sort property: '" + trimmed + "'.");
        }
        return canonical;
    }
}
