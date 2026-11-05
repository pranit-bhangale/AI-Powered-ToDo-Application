package com.pranit.todo.auth.service;

import com.pranit.todo.dto.AuthenticatedUser;
import com.pranit.todo.dto.LoginRequest;
import com.pranit.todo.dto.LoginResponse;
import com.pranit.todo.dto.RegistrationRequest;
import jakarta.validation.Valid;

public interface AuthService {

    void createNewAccount(@Valid RegistrationRequest request);

    LoginResponse authenticateUser(@Valid LoginRequest request);

    AuthenticatedUser getCurrentUser();
}
