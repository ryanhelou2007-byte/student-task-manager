package com.studenttaskmanager.dto;

import com.studenttaskmanager.model.PriorityLevel;
import com.studenttaskmanager.model.TaskCategory;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskCategory category,
        PriorityLevel priority,
        LocalDate dueDate,
        boolean completed,
        Instant createdAt,
        Instant updatedAt
) {
}
