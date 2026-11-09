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
                .map(order -> parseAndValidateOrder(order, allowedSortProperties))
                .toList();
        return Sort.by(orders);
    }

    private static Sort.Order parseAndValidateOrder(
            final Sort.Order order, final Map<String, String> allowedSortProperties) {
        final String rawProperty = order.getProperty();
        Assert.hasText(rawProperty, "Sort property must not be null or blank");

        // Clean out brackets, quotes, and whitespace from Swagger / JSON serialization
        String cleaned = rawProperty.replaceAll("[\\[\\]\"']", "").trim();

        Sort.Direction direction = order.getDirection();
        String propertyName = cleaned;

        // Check if direction was included in the property string (e.g., "createdAt,desc" or "createdAt, desc")
        if (cleaned.contains(",")) {
            final String[] parts = cleaned.split(",", 2);
            propertyName = parts[0].replaceAll("[\\[\\]\"']", "").trim();
            final String dirPart = parts[1].replaceAll("[\\[\\]\"']", "").trim();
            if (!dirPart.isEmpty()) {
                if ("desc".equalsIgnoreCase(dirPart)) {
                    direction = Sort.Direction.DESC;
                } else if ("asc".equalsIgnoreCase(dirPart)) {
                    direction = Sort.Direction.ASC;
                }
            }
        }

        if (propertyName.isBlank()) {
            throw new InvalidRequestException("Sort property must not be blank.");
        }

        final String canonicalProperty = validateSortBy(propertyName, allowedSortProperties);
        return new Sort.Order(direction, canonicalProperty);
    }

    private static String validateSortBy(final String sortBy, final Map<String, String> allowedSortProperties) {
        if (allowedSortProperties.isEmpty()) {
            throw new InvalidRequestException("Sorting is not supported for this query.");
        }
        final String canonical = allowedSortProperties.get(sortBy);
        if (canonical == null) {
            throw new InvalidRequestException("Unsupported sort property: '" + sortBy + "'.");
        }
        return canonical;
    }
}
