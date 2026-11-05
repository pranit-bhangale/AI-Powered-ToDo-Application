package com.pranit.todo.repository;

import com.pranit.todo.entity.Todo;
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
public interface TodoRepository extends JpaRepository<Todo, UUID>, JpaSpecificationExecutor<Todo> {

    @Query("SELECT t FROM Todo t JOIN FETCH t.tasks WHERE t.id = :id")
    Optional<Todo> findByIdWithTasks(@Param("id") UUID id);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId")
    Page<Todo> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Todo t JOIN FETCH t.tasks WHERE t.user.id = :userId")
    Page<Todo> findByUserIdWithTasks(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.title = :title")
    Optional<Todo> findByUserIdAndTitle(@Param("userId") UUID userId, @Param("title") String title);

    boolean existsByUserIdAndTitle(@Param("userId") UUID userId, @Param("title") String title);
}