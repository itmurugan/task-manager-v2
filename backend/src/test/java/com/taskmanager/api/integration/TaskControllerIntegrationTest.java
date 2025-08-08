package com.taskmanager.api.integration;

import com.taskmanager.api.config.TestSecurityConfig;
import com.taskmanager.api.dto.TaskCreateRequest;
import com.taskmanager.api.dto.TaskDTO;
import com.taskmanager.api.entity.Task;
import com.taskmanager.api.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Task Controller using TestRestTemplate
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Task Controller Integration Tests")
class TaskControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/tasks";
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve task successfully")
    void createAndRetrieveTask_ShouldWorkEndToEnd() {
        // Create task
        TaskCreateRequest createRequest = new TaskCreateRequest("Integration Test Task", "Test Description");
        
        ResponseEntity<TaskDTO> createResponse = restTemplate.postForEntity(
                baseUrl, 
                createRequest, 
                TaskDTO.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getTitle()).isEqualTo("Integration Test Task");
        assertThat(createResponse.getBody().getCompleted()).isFalse();
        
        Long taskId = createResponse.getBody().getId();
        
        // Retrieve task
        ResponseEntity<TaskDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + taskId, 
                TaskDTO.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(taskId);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Integration Test Task");
    }

    @Test
    @DisplayName("Should get all tasks")
    void getAllTasks_ShouldReturnAllTasks() {
        // Create test tasks
        taskRepository.save(new Task("Task 1", "Description 1"));
        taskRepository.save(new Task("Task 2", "Description 2"));
        
        ResponseEntity<List<TaskDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TaskDTO>>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle task completion status changes")
    void taskCompletion_ShouldUpdateCorrectly() {
        // Create a task
        Task task = taskRepository.save(new Task("Test Task", "Test Description"));
        Long taskId = task.getId();
        
        // Mark as completed
        ResponseEntity<TaskDTO> completeResponse = restTemplate.exchange(
                baseUrl + "/" + taskId + "/complete",
                HttpMethod.PUT,
                null,
                TaskDTO.class);
        
        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completeResponse.getBody()).isNotNull();
        assertThat(completeResponse.getBody().getCompleted()).isTrue();
        
        // Mark as incomplete
        ResponseEntity<TaskDTO> incompleteResponse = restTemplate.exchange(
                baseUrl + "/" + taskId + "/incomplete",
                HttpMethod.PUT,
                null,
                TaskDTO.class);
        
        assertThat(incompleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(incompleteResponse.getBody()).isNotNull();
        assertThat(incompleteResponse.getBody().getCompleted()).isFalse();
    }

    @Test
    @DisplayName("Should delete task successfully")
    void deleteTask_ShouldRemoveTask() {
        // Create a task
        Task task = taskRepository.save(new Task("Delete Test Task", "To be deleted"));
        Long taskId = task.getId();
        
        // Delete the task
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + taskId,
                HttpMethod.DELETE,
                null,
                Void.class);
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify task is deleted
        ResponseEntity<TaskDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + taskId,
                TaskDTO.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should handle non-existent task operations")
    void nonExistentTask_ShouldReturn404() {
        Long nonExistentId = 99999L;
        
        // Try to get non-existent task
        ResponseEntity<TaskDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + nonExistentId,
                TaskDTO.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        // Try to delete non-existent task
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.DELETE,
                null,
                Void.class);
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should validate task creation")
    void taskValidation_ShouldReturnBadRequestForInvalidData() {
        // Test empty title
        TaskCreateRequest invalidRequest = new TaskCreateRequest("", "Valid description");
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                invalidRequest,
                String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}