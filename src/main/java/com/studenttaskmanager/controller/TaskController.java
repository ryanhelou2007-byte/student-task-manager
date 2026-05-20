package com.studenttaskmanager.controller;

import com.studenttaskmanager.dto.CompletionRequest;
import com.studenttaskmanager.dto.TaskRequest;
import com.studenttaskmanager.dto.TaskResponse;
import com.studenttaskmanager.security.AuthenticatedUser;
import com.studenttaskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> listTasks(@AuthenticationPrincipal AuthenticatedUser user) {
        return taskService.listTasks(user.id());
    }

    @GetMapping("/upcoming")
    public List<TaskResponse> listUpcomingTasks(@AuthenticationPrincipal AuthenticatedUser user) {
        return taskService.listUpcomingTasks(user.id());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.status(201).body(taskService.createTask(user.id(), request));
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request
    ) {
        return taskService.updateTask(user.id(), taskId, request);
    }

    @PatchMapping("/{taskId}/completion")
    public TaskResponse updateCompletion(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long taskId,
            @Valid @RequestBody CompletionRequest request
    ) {
        return taskService.updateCompletion(user.id(), taskId, request);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long taskId
    ) {
        taskService.deleteTask(user.id(), taskId);
        return ResponseEntity.noContent().build();
    }
}
