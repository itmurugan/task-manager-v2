package com.taskmanager.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.api.config.TestSecurityConfig;
import com.taskmanager.api.dto.TaskCreateRequest;
import com.taskmanager.api.dto.TaskDTO;
import com.taskmanager.api.dto.TaskUpdateRequest;
import com.taskmanager.api.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController
 */
@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Task Controller Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskDTO sampleTaskDTO;
    private TaskCreateRequest createRequest;
    private TaskUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        sampleTaskDTO = new TaskDTO();
        sampleTaskDTO.setId(1L);
        sampleTaskDTO.setTitle("Test Task");
        sampleTaskDTO.setDescription("Test Description");
        sampleTaskDTO.setCompleted(false);
        sampleTaskDTO.setCreatedAt(LocalDateTime.now());
        sampleTaskDTO.setUpdatedAt(LocalDateTime.now());

        createRequest = new TaskCreateRequest("New Task", "New Description");
        updateRequest = new TaskUpdateRequest("Updated Task", "Updated Description", true);
    }

    @Test
    @DisplayName("GET /tasks should return all tasks")
    void getAllTasks_ShouldReturnTaskList() throws Exception {
        // Given
        List<TaskDTO> tasks = Arrays.asList(sampleTaskDTO);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    @DisplayName("GET /tasks/{id} should return specific task")
    void getTaskById_WithValidId_ShouldReturnTask() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(sampleTaskDTO);

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("GET /tasks/{id} should return 404 for non-existent task")
    void getTaskById_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenThrow(new EntityNotFoundException("Task not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }

    @Test
    @DisplayName("POST /tasks should create new task")
    void createTask_WithValidRequest_ShouldCreateTask() throws Exception {
        // Given
        when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(sampleTaskDTO);

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /tasks should return 400 for invalid request")
    void createTask_WithInvalidRequest_ShouldReturn400() throws Exception {
        // Given
        TaskCreateRequest invalidRequest = new TaskCreateRequest("", "Description");

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("PUT /tasks/{id} should update existing task")
    void updateTask_WithValidRequest_ShouldUpdateTask() throws Exception {
        // Given
        TaskDTO updatedTask = new TaskDTO();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setCompleted(true);
        
        when(taskService.updateTask(eq(1L), any(TaskUpdateRequest.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("PUT /tasks/{id}/complete should mark task as completed")
    void markTaskAsCompleted_WithValidId_ShouldCompleteTask() throws Exception {
        // Given
        TaskDTO completedTask = new TaskDTO();
        completedTask.setId(1L);
        completedTask.setTitle("Test Task");
        completedTask.setCompleted(true);
        
        when(taskService.markTaskAsCompleted(1L)).thenReturn(completedTask);

        // When & Then
        mockMvc.perform(put("/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("PUT /tasks/{id}/incomplete should mark task as incomplete")
    void markTaskAsIncomplete_WithValidId_ShouldMarkIncomplete() throws Exception {
        // Given
        TaskDTO incompleteTask = new TaskDTO();
        incompleteTask.setId(1L);
        incompleteTask.setTitle("Test Task");
        incompleteTask.setCompleted(false);
        
        when(taskService.markTaskAsIncomplete(1L)).thenReturn(incompleteTask);

        // When & Then
        mockMvc.perform(put("/tasks/1/incomplete"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} should delete task")
    void deleteTask_WithValidId_ShouldDeleteTask() throws Exception {
        // When & Then
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /tasks/completed should return completed tasks")
    void getCompletedTasks_ShouldReturnCompletedTasks() throws Exception {
        // Given
        TaskDTO completedTask = new TaskDTO();
        completedTask.setCompleted(true);
        List<TaskDTO> completedTasks = Arrays.asList(completedTask);
        
        when(taskService.getCompletedTasks()).thenReturn(completedTasks);

        // When & Then
        mockMvc.perform(get("/tasks/completed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    @DisplayName("GET /tasks/incomplete should return incomplete tasks")
    void getIncompleteTasks_ShouldReturnIncompleteTasks() throws Exception {
        // Given
        List<TaskDTO> incompleteTasks = Arrays.asList(sampleTaskDTO);
        when(taskService.getIncompleteTasks()).thenReturn(incompleteTasks);

        // When & Then
        mockMvc.perform(get("/tasks/incomplete"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    @DisplayName("GET /tasks/search should return matching tasks")
    void searchTasks_WithTitle_ShouldReturnMatchingTasks() throws Exception {
        // Given
        List<TaskDTO> matchingTasks = Arrays.asList(sampleTaskDTO);
        when(taskService.searchTasksByTitle("Test")).thenReturn(matchingTasks);

        // When & Then
        mockMvc.perform(get("/tasks/search")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /tasks/statistics should return task statistics")
    void getTaskStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        TaskService.TaskStatistics stats = new TaskService.TaskStatistics(10L, 4L, 6L);
        when(taskService.getTaskStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/tasks/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTasks").value(10))
                .andExpect(jsonPath("$.completedTasks").value(4))
                .andExpect(jsonPath("$.incompleteTasks").value(6));
    }
}