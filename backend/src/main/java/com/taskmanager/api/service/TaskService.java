package com.taskmanager.api.service;

import com.taskmanager.api.dto.TaskCreateRequest;
import com.taskmanager.api.dto.TaskDTO;
import com.taskmanager.api.dto.TaskUpdateRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing tasks
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get all tasks ordered by creation date (newest first)
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    /**
     * Create a new task
     */
    public TaskDTO createTask(TaskCreateRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(false);
        
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    /**
     * Update an existing task
     */
    public TaskDTO updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription().trim());
        }
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    /**
     * Mark task as completed
     */
    public TaskDTO markTaskAsCompleted(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        
        task.setCompleted(true);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    /**
     * Mark task as incomplete
     */
    public TaskDTO markTaskAsIncomplete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        
        task.setCompleted(false);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    /**
     * Delete a task
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Get completed tasks
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getCompletedTasks() {
        return taskRepository.findByCompletedTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get incomplete tasks
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getIncompleteTasks() {
        return taskRepository.findByCompletedFalse()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search tasks by title
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get task statistics
     */
    @Transactional(readOnly = true)
    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByCompletedTrue();
        long incompleteTasks = taskRepository.countByCompletedFalse();
        
        return new TaskStatistics(totalTasks, completedTasks, incompleteTasks);
    }

    /**
     * Convert Task entity to TaskDTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCompleted(task.getCompleted());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    /**
     * Inner class for task statistics
     */
    public static class TaskStatistics {
        private final long totalTasks;
        private final long completedTasks;
        private final long incompleteTasks;

        public TaskStatistics(long totalTasks, long completedTasks, long incompleteTasks) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.incompleteTasks = incompleteTasks;
        }

        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getIncompleteTasks() { return incompleteTasks; }
    }
}