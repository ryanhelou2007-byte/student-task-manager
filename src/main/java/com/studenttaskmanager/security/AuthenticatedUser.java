package com.studenttaskmanager.security;

public record AuthenticatedUser(Long id, String email, String displayName) {
}
