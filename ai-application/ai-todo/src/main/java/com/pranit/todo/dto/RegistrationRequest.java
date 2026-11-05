package com.pranit.todo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword

) {
    public RegistrationRequest {
        if (password != null && confirmPassword != null
                && !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
    }
}
