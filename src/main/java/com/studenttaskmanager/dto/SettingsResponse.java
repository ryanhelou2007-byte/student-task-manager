package com.studenttaskmanager.dto;

import com.studenttaskmanager.model.DigestFrequency;

public record SettingsResponse(
        DigestFrequency digestFrequency,
        boolean examReminders,
        boolean assignmentReminders,
        String timezone
) {
}
