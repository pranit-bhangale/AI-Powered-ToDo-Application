package com.pranit.todo.repository;

import com.pranit.todo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t JOIN FETCH t.todo WHERE t.id = :id AND t.todo.id = :todoId")
    Optional<Task> findByIdAndTodoId(@Param("id") UUID id, @Param("todoId") UUID todoId);

    @Query("SELECT t FROM Task t WHERE t.todo.id = :todoId")
    Page<Task> findByTodoId(@Param("todoId") UUID todoId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.todo.id = :todoId AND t.title = :title")
    Optional<Task> findByTodoIdAndTitle(@Param("todoId") UUID todoId, @Param("title") String title);

    boolean existsByTodoIdAndTitle(@Param("todoId") UUID todoId, @Param("title") String title);

    @Query("SELECT t FROM Task t WHERE t.todo.id = :todoId")
    List<Task> findAllByTodoId(@Param("todoId") UUID todoId);

    long countByTodoId(@Param("todoId") UUID todoId);

    long countByTodoIdAndCompleted(@Param("todoId") UUID todoId, @Param("completed") boolean completed);
}