package com.studenttaskmanager.service;

import com.studenttaskmanager.dto.CompletionRequest;
import com.studenttaskmanager.dto.TaskRequest;
import com.studenttaskmanager.dto.TaskResponse;
import com.studenttaskmanager.model.TaskItem;
import com.studenttaskmanager.model.UserAccount;
import com.studenttaskmanager.repository.TaskItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskItemRepository taskItemRepository;
    private final UserService userService;

    public TaskService(TaskItemRepository taskItemRepository, UserService userService) {
        this.taskItemRepository = taskItemRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long userId) {
        return taskItemRepository.findByUserIdOrderByDueDateAscCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listUpcomingTasks(Long userId) {
        LocalDate today = LocalDate.now();
        return taskItemRepository.findByUserIdAndDueDateBetweenOrderByDueDateAscCreatedAtDesc(
                        userId,
                        today,
                        today.plusDays(7)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        UserAccount user = userService.findById(userId);

        TaskItem task = new TaskItem();
        task.setUser(user);
        applyRequest(task, request);
        task.setCompleted(Boolean.TRUE.equals(request.completed()));

        return toResponse(taskItemRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        TaskItem task = findOwnedTask(userId, taskId);
        applyRequest(task, request);

        if (request.completed() != null) {
            task.setCompleted(request.completed());
        }

        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateCompletion(Long userId, Long taskId, CompletionRequest request) {
        TaskItem task = findOwnedTask(userId, taskId);
        task.setCompleted(request.completed());
        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        TaskItem task = findOwnedTask(userId, taskId);
        taskItemRepository.delete(task);
    }

    private TaskItem findOwnedTask(Long userId, Long taskId) {
        return taskItemRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private void applyRequest(TaskItem task, TaskRequest request) {
        task.setTitle(request.title().trim());
        task.setDescription(request.description() == null ? "" : request.description().trim());
        task.setCategory(request.category());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
    }

    private TaskResponse toResponse(TaskItem task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
