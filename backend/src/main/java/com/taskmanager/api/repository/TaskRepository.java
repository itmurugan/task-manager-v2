package com.taskmanager.api.repository;

import com.taskmanager.api.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks ordered by creation date descending
     */
    @Query("SELECT t FROM Task t ORDER BY t.createdAt DESC")
    List<Task> findAllOrderByCreatedAtDesc();

    /**
     * Find all completed tasks
     */
    List<Task> findByCompletedTrue();

    /**
     * Find all incomplete tasks
     */
    List<Task> findByCompletedFalse();

    /**
     * Find tasks by title containing the given string (case insensitive)
     */
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> findByTitleContainingIgnoreCase(String title);

    /**
     * Count completed tasks
     */
    long countByCompletedTrue();

    /**
     * Count incomplete tasks
     */
    long countByCompletedFalse();
}