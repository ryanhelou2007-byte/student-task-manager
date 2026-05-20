package com.studenttaskmanager.controller;

import com.studenttaskmanager.dto.SettingsRequest;
import com.studenttaskmanager.dto.SettingsResponse;
import com.studenttaskmanager.security.AuthenticatedUser;
import com.studenttaskmanager.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse getSettings(@AuthenticationPrincipal AuthenticatedUser user) {
        return settingsService.getSettings(user.id());
    }

    @PutMapping
    public SettingsResponse updateSettings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SettingsRequest request
    ) {
        return settingsService.updateSettings(user.id(), request);
    }
}
