package com.taskmanager.api.controller;

import com.taskmanager.api.dto.TaskCreateRequest;
import com.taskmanager.api.dto.TaskDTO;
import com.taskmanager.api.dto.TaskUpdateRequest;
import com.taskmanager.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Task management operations
 */
@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieve all tasks ordered by creation date (newest first)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the task"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> getTaskById(
            @Parameter(description = "ID of the task to retrieve") @PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Create a new task with title and description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid task data provided")
    })
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskDTO createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Update an existing task with new data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid task data provided")
    })
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "ID of the task to update") @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskDTO updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark task as completed", description = "Mark a specific task as completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task marked as completed"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> markTaskAsCompleted(
            @Parameter(description = "ID of the task to complete") @PathVariable Long id) {
        TaskDTO completedTask = taskService.markTaskAsCompleted(id);
        return ResponseEntity.ok(completedTask);
    }

    @PutMapping("/{id}/incomplete")
    @Operation(summary = "Mark task as incomplete", description = "Mark a specific task as incomplete")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task marked as incomplete"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> markTaskAsIncomplete(
            @Parameter(description = "ID of the task to mark as incomplete") @PathVariable Long id) {
        TaskDTO incompleteTask = taskService.markTaskAsIncomplete(id);
        return ResponseEntity.ok(incompleteTask);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Delete a specific task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the task to delete") @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed tasks", description = "Retrieve all completed tasks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved completed tasks")
    public ResponseEntity<List<TaskDTO>> getCompletedTasks() {
        List<TaskDTO> completedTasks = taskService.getCompletedTasks();
        return ResponseEntity.ok(completedTasks);
    }

    @GetMapping("/incomplete")
    @Operation(summary = "Get incomplete tasks", description = "Retrieve all incomplete tasks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved incomplete tasks")
    public ResponseEntity<List<TaskDTO>> getIncompleteTasks() {
        List<TaskDTO> incompleteTasks = taskService.getIncompleteTasks();
        return ResponseEntity.ok(incompleteTasks);
    }

    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Search tasks by title (case insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching tasks")
    public ResponseEntity<List<TaskDTO>> searchTasks(
            @Parameter(description = "Title to search for") @RequestParam String title) {
        List<TaskDTO> matchingTasks = taskService.searchTasksByTitle(title);
        return ResponseEntity.ok(matchingTasks);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get task statistics", description = "Retrieve statistics about tasks (total, completed, incomplete)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved task statistics")
    public ResponseEntity<TaskService.TaskStatistics> getTaskStatistics() {
        TaskService.TaskStatistics statistics = taskService.getTaskStatistics();
        return ResponseEntity.ok(statistics);
    }
}