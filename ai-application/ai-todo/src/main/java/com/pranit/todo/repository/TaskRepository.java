package com.pranit.todo.repository;

import com.pranit.todo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT t
            FROM Task t
            JOIN FETCH t.todo
            WHERE t.id = :id
            AND t.todo.id = :todoId
            AND t.todo.user.id = :userId
            """)
    Optional<Task> findByIdAndTodoIdAndUserId
            (@Param("id") UUID id, @Param("todoId") UUID todoId, @Param("userId") UUID userId);

    @Query(
            value = """
                    SELECT t
                    FROM Task t
                    WHERE t.todo.id = :todoId
                    AND t.todo.user.id = :userId
                    """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM Task t
                    WHERE t.todo.id = :todoId
                    AND t.todo.user.id = :userId
                    """
    )
    Page<Task> findByTodoIdAndUserId
            (@Param("todoId") UUID todoId, @Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.todo.id = :todoId
            AND t.title = :title
            """)
    Optional<Task> findByTodoIdAndTitle
            (@Param("todoId") UUID todoId, @Param("title") String title);

    boolean existsByTodoIdAndTitle(@Param("todoId") UUID todoId, @Param("title") String title);

    boolean existsByTodoIdAndCompletedFalse(UUID todoId);

    @Modifying
    @Query("""
            DELETE FROM Task t WHERE t.todo.id = :todoId
            """)
    void deleteByTodoId(@Param("todoId") UUID todoId);
}