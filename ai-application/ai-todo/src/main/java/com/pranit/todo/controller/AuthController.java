package com.pranit.todo.controller;

import com.pranit.todo.auth.service.AuthService;
import com.pranit.todo.dto.AuthenticatedUser;
import com.pranit.todo.dto.LoginRequest;
import com.pranit.todo.dto.LoginResponse;
import com.pranit.todo.dto.RegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and session management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new account", description = "Creates a new user account with username, email, and password.")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or password mismatch")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    @PostMapping(value = "/register", version = "v1")
    public ResponseEntity<Map<String, String>> createNewAccount(@RequestBody @Valid RegistrationRequest registrationRequest) {
        this.authService.createNewAccount(registrationRequest);
        return ResponseEntity.created(URI.create("/api/auth/login"))
                .body(Map.of("message", "Account created successfully. Please log in to continue."));
    }

    @Operation(summary = "Authenticate user", description = "Authenticates credentials and returns a signed JWT access token.")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping(value = "/login", version = "v1")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(this.authService.authenticateUser(loginRequest));
    }

    @Operation(summary = "Get current user profile", description = "Retrieves profile and roles for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Current user retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping(value = "/me", version = "v1")
    public ResponseEntity<AuthenticatedUser> getCurrentUser() {
        return ResponseEntity.ok(this.authService.getCurrentUser());
    }

    @Operation(summary = "Logout user", description = "Logs out the current user session.")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @PostMapping(value = "/logout", version = "v1")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
