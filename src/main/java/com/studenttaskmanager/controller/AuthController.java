package com.studenttaskmanager.controller;

import com.studenttaskmanager.dto.ProviderAuthRequest;
import com.studenttaskmanager.dto.SignInRequest;
import com.studenttaskmanager.dto.SignUpRequest;
import com.studenttaskmanager.dto.UserResponse;
import com.studenttaskmanager.model.UserAccount;
import com.studenttaskmanager.security.AuthSession;
import com.studenttaskmanager.security.AuthenticatedUser;
import com.studenttaskmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponse signUp(@Valid @RequestBody SignUpRequest request, HttpServletRequest servletRequest) {
        UserAccount user = userService.register(request);
        authenticateSession(servletRequest, user);
        return userService.toResponse(user);
    }

    @PostMapping("/signin")
    public UserResponse signIn(@Valid @RequestBody SignInRequest request, HttpServletRequest servletRequest) {
        UserAccount user = userService.authenticate(request);
        authenticateSession(servletRequest, user);
        return userService.toResponse(user);
    }

    @PostMapping("/provider/{provider}")
    public ResponseEntity<Map<String, String>> providerSignIn(
            @PathVariable String provider,
            @Valid @RequestBody ProviderAuthRequest request
    ) {
        String normalizedProvider = provider.toLowerCase(Locale.ROOT);
        if (!normalizedProvider.equals("google") && !normalizedProvider.equals("github")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found.");
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message",
                "External " + normalizedProvider + " sign-in is disabled because OAuth credentials are not configured for this deployment."
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(@AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAccount user = userService.findById(principal.id());
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private void authenticateSession(HttpServletRequest request, UserAccount user) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(AuthSession.USER_ID, user.getId());
    }
}
