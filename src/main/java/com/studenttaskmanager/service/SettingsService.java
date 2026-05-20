package com.studenttaskmanager.service;

import com.studenttaskmanager.dto.SettingsRequest;
import com.studenttaskmanager.dto.SettingsResponse;
import com.studenttaskmanager.model.UserAccount;
import com.studenttaskmanager.model.UserSettings;
import com.studenttaskmanager.repository.UserSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class SettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserService userService;

    public SettingsService(UserSettingsRepository userSettingsRepository, UserService userService) {
        this.userSettingsRepository = userSettingsRepository;
        this.userService = userService;
    }

    @Transactional
    public SettingsResponse getSettings(Long userId) {
        return toResponse(findOrCreateSettings(userId));
    }

    @Transactional
    public SettingsResponse updateSettings(Long userId, SettingsRequest request) {
        validateTimezone(request.timezone());
        UserSettings settings = findOrCreateSettings(userId);
        settings.setDigestFrequency(request.digestFrequency());
        settings.setExamReminders(request.examReminders());
        settings.setAssignmentReminders(request.assignmentReminders());
        settings.setTimezone(request.timezone());
        return toResponse(settings);
    }

    private UserSettings findOrCreateSettings(Long userId) {
        return userSettingsRepository.findByUserId(userId).orElseGet(() -> {
            UserAccount user = userService.findById(userId);
            UserSettings settings = new UserSettings();
            settings.setUser(user);
            return userSettingsRepository.save(settings);
        });
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a valid timezone.");
        }
    }

    private SettingsResponse toResponse(UserSettings settings) {
        return new SettingsResponse(
                settings.getDigestFrequency(),
                settings.isExamReminders(),
                settings.isAssignmentReminders(),
                settings.getTimezone()
        );
    }
}
