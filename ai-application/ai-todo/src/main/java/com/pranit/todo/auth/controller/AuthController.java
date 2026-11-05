package com.pranit.todo.auth.controller;

import com.pranit.todo.auth.service.AuthService;
import com.pranit.todo.dto.AuthenticatedUser;
import com.pranit.todo.dto.LoginRequest;
import com.pranit.todo.dto.LoginResponse;
import com.pranit.todo.dto.RegistrationRequest;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", version = "v1")
    public ResponseEntity<String> createNewAccount(@RequestBody @Valid RegistrationRequest registrationRequest) {
        this.authService.createNewAccount(registrationRequest);
        return ResponseEntity.created(URI.create("/api/auth/login"))
                .body("Account Created Successfully");
    }

    @PostMapping(value = "/login", version = "v1")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(this.authService.authenticateUser(loginRequest));
    }

    @GetMapping(value = "/me", version = "v1")
    public ResponseEntity<AuthenticatedUser> getCurrentUser() {
        return ResponseEntity.ok(this.authService.getCurrentUser());
    }

    @PostMapping(value = "/logout", version = "v1")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
