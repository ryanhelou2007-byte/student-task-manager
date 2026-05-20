package com.studenttaskmanager.dto;

import com.studenttaskmanager.model.DigestFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SettingsRequest(
        @NotNull DigestFrequency digestFrequency,
        boolean examReminders,
        boolean assignmentReminders,
        @NotBlank @Size(max = 80) String timezone
) {
}
