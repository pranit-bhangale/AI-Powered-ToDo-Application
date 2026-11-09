package com.pranit.todo.wrapper;

import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a paginated response containing mapped content and pagination metadata.
 *
 * @param contents      the content of the current page
 * @param currentPage   the zero-based index of the current page
 * @param pageSize      the number of elements per page
 * @param totalElements the total number of elements across all pages
 * @param totalPages    the total number of pages
 * @param isFirstPage   whether the current page is the first page
 * @param isLastPage    whether the current page is the last page
 * @param hasNext       whether there is a next page
 * @param hasPrevious   whether there is a previous page
 * @param <T>           the type of content elements
 */
public record PageResponse<T>(
        List<T> contents,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirstPage,
        boolean isLastPage,
        boolean hasNext,
        boolean hasPrevious
) {

    public PageResponse {
        contents = contents == null ? List.of() : List.copyOf(contents);
        Assert.isTrue(currentPage >= 0, "Current page must be >= 0");
        Assert.isTrue(pageSize >= 0, "Page size must be >= 0");
        Assert.isTrue(totalElements >= 0, "Total elements must be >= 0");
        Assert.isTrue(totalPages >= 0, "Total pages must be >= 0");
    }

    public static <T> PageResponse<T> from(final Page<T> page) {
        return from(page, Function.identity());
    }

    public static <S, T> PageResponse<T> from(
            final Page<S> page, final Function<? super S, ? extends T> mapper
    ) {
        Assert.notNull(page, "Page must not be null");
        Assert.notNull(mapper, "Mapper must not be null");
        final List<T> contents = page.getContent()
                .stream()
                .<T>map(mapper)
                .toList();
        return new PageResponse<>(
                contents,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
