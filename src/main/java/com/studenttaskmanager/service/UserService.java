package com.studenttaskmanager.service;

import com.studenttaskmanager.dto.SignInRequest;
import com.studenttaskmanager.dto.SignUpRequest;
import com.studenttaskmanager.dto.UserResponse;
import com.studenttaskmanager.model.AuthProvider;
import com.studenttaskmanager.model.UserAccount;
import com.studenttaskmanager.model.UserSettings;
import com.studenttaskmanager.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(SignUpRequest request) {
        String email = normalizeEmail(request.email());

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(resolveDisplayName(request.displayName(), email));
        user.setAuthProvider(AuthProvider.LOCAL);

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        user.setSettings(settings);

        return userAccountRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(SignInRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserAccount findById(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));
    }

    public UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }

    private String resolveDisplayName(String requestedDisplayName, String email) {
        if (requestedDisplayName != null && !requestedDisplayName.isBlank()) {
            return requestedDisplayName.trim();
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : "Student";
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
