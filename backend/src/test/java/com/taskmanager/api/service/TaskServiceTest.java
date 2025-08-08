package com.taskmanager.api.service;

import com.taskmanager.api.dto.TaskCreateRequest;
import com.taskmanager.api.dto.TaskDTO;
import com.taskmanager.api.dto.TaskUpdateRequest;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Task Service Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private TaskCreateRequest createRequest;
    private TaskUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setCompleted(false);
        sampleTask.setCreatedAt(LocalDateTime.now());
        sampleTask.setUpdatedAt(LocalDateTime.now());

        createRequest = new TaskCreateRequest("New Task", "New Description");
        updateRequest = new TaskUpdateRequest("Updated Task", "Updated Description", true);
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void getAllTasks_ShouldReturnTaskList() {
        // Given
        List<Task> tasks = Arrays.asList(sampleTask);
        when(taskRepository.findAllOrderByCreatedAtDesc()).thenReturn(tasks);

        // When
        List<TaskDTO> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void getTaskById_WithValidId_ShouldReturnTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        // When
        TaskDTO result = taskService.getTaskById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found by ID")
    void getTaskById_WithInvalidId_ShouldThrowException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskRepository).findById(999L);
    }

    @Test
    @DisplayName("Should create task successfully")
    void createTask_WithValidRequest_ShouldReturnCreatedTask() {
        // Given
        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(createRequest.getTitle());
        savedTask.setDescription(createRequest.getDescription());
        savedTask.setCompleted(false);
        savedTask.setCreatedAt(LocalDateTime.now());
        savedTask.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        TaskDTO result = taskService.createTask(createRequest);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getCompleted()).isFalse();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void updateTask_WithValidRequest_ShouldReturnUpdatedTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // When
        TaskDTO result = taskService.updateTask(1L, updateRequest);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent task")
    void updateTask_WithInvalidId_ShouldThrowException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(999L, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should mark task as completed successfully")
    void markTaskAsCompleted_WithValidId_ShouldReturnCompletedTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // When
        TaskDTO result = taskService.markTaskAsCompleted(1L);

        // Then
        assertThat(sampleTask.getCompleted()).isTrue();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Should mark task as incomplete successfully")
    void markTaskAsIncomplete_WithValidId_ShouldReturnIncompleteTask() {
        // Given
        sampleTask.setCompleted(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // When
        TaskDTO result = taskService.markTaskAsIncomplete(1L);

        // Then
        assertThat(sampleTask.getCompleted()).isFalse();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void deleteTask_WithValidId_ShouldDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent task")
    void deleteTask_WithInvalidId_ShouldThrowException() {
        // Given
        when(taskRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get completed tasks successfully")
    void getCompletedTasks_ShouldReturnCompletedTasks() {
        // Given
        Task completedTask = new Task();
        completedTask.setCompleted(true);
        List<Task> completedTasks = Arrays.asList(completedTask);
        when(taskRepository.findByCompletedTrue()).thenReturn(completedTasks);

        // When
        List<TaskDTO> result = taskService.getCompletedTasks();

        // Then
        assertThat(result).hasSize(1);
        verify(taskRepository).findByCompletedTrue();
    }

    @Test
    @DisplayName("Should get incomplete tasks successfully")
    void getIncompleteTasks_ShouldReturnIncompleteTasks() {
        // Given
        List<Task> incompleteTasks = Arrays.asList(sampleTask);
        when(taskRepository.findByCompletedFalse()).thenReturn(incompleteTasks);

        // When
        List<TaskDTO> result = taskService.getIncompleteTasks();

        // Then
        assertThat(result).hasSize(1);
        verify(taskRepository).findByCompletedFalse();
    }

    @Test
    @DisplayName("Should search tasks by title successfully")
    void searchTasksByTitle_WithValidTitle_ShouldReturnMatchingTasks() {
        // Given
        List<Task> matchingTasks = Arrays.asList(sampleTask);
        when(taskRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingTasks);

        // When
        List<TaskDTO> result = taskService.searchTasksByTitle("Test");

        // Then
        assertThat(result).hasSize(1);
        verify(taskRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Should get task statistics successfully")
    void getTaskStatistics_ShouldReturnCorrectStatistics() {
        // Given
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByCompletedTrue()).thenReturn(4L);
        when(taskRepository.countByCompletedFalse()).thenReturn(6L);

        // When
        TaskService.TaskStatistics result = taskService.getTaskStatistics();

        // Then
        assertThat(result.getTotalTasks()).isEqualTo(10L);
        assertThat(result.getCompletedTasks()).isEqualTo(4L);
        assertThat(result.getIncompleteTasks()).isEqualTo(6L);
        verify(taskRepository).count();
        verify(taskRepository).countByCompletedTrue();
        verify(taskRepository).countByCompletedFalse();
    }
}