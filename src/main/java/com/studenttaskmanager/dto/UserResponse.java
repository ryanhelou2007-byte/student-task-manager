package com.studenttaskmanager.dto;

public record UserResponse(
        Long id,
        String email,
        String displayName
) {
}
