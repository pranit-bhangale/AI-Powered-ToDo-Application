package com.pranit.todo.entity;

import com.pranit.todo.exception.InvalidRequestException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "todos",
        indexes = {
                @Index(name = "idx_todos_user_id", columnList = "user_id"),
                @Index(name = "idx_todos_user_completed", columnList = "user_id, completed"),
                @Index(name = "idx_todos_user_created", columnList = "user_id, created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_todos_user_title", columnNames = {"user_id", "title"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 200)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;

    @Builder.Default
    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "todo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setTodo(this);
        if (!task.isCompleted()) {
            this.completed = false;
        }
    }

    public boolean hasIncompleteTasks() {
        return tasks != null && tasks.stream().anyMatch(task -> !task.isCompleted());
    }

    public void complete() {
        if (!canBeCompleted()) {
            throw new InvalidRequestException("Cannot complete Todo: all tasks in this todo must be completed first.");
        }
        this.completed = true;
    }

    public boolean canBeCompleted() {
        return tasks == null || tasks.isEmpty() || tasks.stream().allMatch(Task::isCompleted);
    }

    public void reopen() {
        this.completed = false;
    }
}