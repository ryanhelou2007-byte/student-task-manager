package com.studenttaskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProviderAuthRequest(
        @Email @NotBlank String email
) {
}
