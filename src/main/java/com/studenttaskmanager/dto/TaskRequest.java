package com.studenttaskmanager.dto;

import com.studenttaskmanager.model.PriorityLevel;
import com.studenttaskmanager.model.TaskCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 140) String title,
        @Size(max = 1200) String description,
        @NotNull TaskCategory category,
        @NotNull PriorityLevel priority,
        @NotNull LocalDate dueDate,
        Boolean completed
) {
}
