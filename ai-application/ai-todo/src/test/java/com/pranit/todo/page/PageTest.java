package com.pranit.todo.page;

import com.pranit.todo.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageTest {

    private static final Set<String> ALLOWED_PROPERTIES = Set.of(
            "createdAt", "updatedAt", "title", "completed", "startDate", "endDate"
    );

    @Test
    @DisplayName("Sanitizes Swagger JSON array formatted sort property [\"createdAt\"]")
    void validatePageable_sanitizesSwaggerJsonArraySort() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "[\"createdAt\"]");
        final Pageable pageable = PageRequest.of(0, 10, sort);

        final Pageable validated = Page.validatePageable(pageable, ALLOWED_PROPERTIES);

        assertThat(validated.getSort().isSorted()).isTrue();
        final Sort.Order order = validated.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getProperty()).isEqualTo("createdAt");
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("Sanitizes combined property and direction like [\"createdAt,desc\"]")
    void validatePageable_sanitizesCombinedPropertyAndDirection() {
        final Sort sort = Sort.by("[\"createdAt,desc\"]");
        final Pageable pageable = PageRequest.of(0, 10, sort);

        final Pageable validated = Page.validatePageable(pageable, ALLOWED_PROPERTIES);

        final Sort.Order order = validated.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getProperty()).isEqualTo("createdAt");
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("Sanitizes single quotes and brackets like ['title']")
    void validatePageable_sanitizesSingleQuotesAndBrackets() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "['title']");
        final Pageable pageable = PageRequest.of(0, 10, sort);

        final Pageable validated = Page.validatePageable(pageable, ALLOWED_PROPERTIES);

        final Sort.Order order = validated.getSort().getOrderFor("title");
        assertThat(order).isNotNull();
        assertThat(order.getProperty()).isEqualTo("title");
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("Throws InvalidRequestException for unsupported sort property")
    void validatePageable_throwsOnUnsupportedSortProperty() {
        final Sort sort = Sort.by("[\"invalidProperty\"]");
        final Pageable pageable = PageRequest.of(0, 10, sort);

        assertThatThrownBy(() -> Page.validatePageable(pageable, ALLOWED_PROPERTIES))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Unsupported sort property: 'invalidProperty'");
    }
}
