package com.pranit.todo.repository;

import com.pranit.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {

    @Query("""
            SELECT t
            FROM Todo t
            LEFT JOIN FETCH t.tasks
            WHERE t.id = :id
            AND t.user.id = :userId
            """)
    Optional<Todo> findByIdAndUserIdWithTasks(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Todo> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query(
            value = """
                    SELECT t FROM Todo t WHERE t.user.id = :userId
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Todo t WHERE t.user.id = :userId
                    """
    )
    Page<Todo> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByUserIdAndTitle(@Param("userId") UUID userId, @Param("title") String title);
}