package com.pranit.todo.auth.service.impl;

import com.pranit.todo.auth.service.AuthService;
import com.pranit.todo.dto.AuthenticatedUser;
import com.pranit.todo.dto.LoginRequest;
import com.pranit.todo.dto.LoginResponse;
import com.pranit.todo.dto.RegistrationRequest;
import com.pranit.todo.dto.TokenResponse;
import com.pranit.todo.entity.User;
import com.pranit.todo.exception.DuplicateResourceException;
import com.pranit.todo.model.UserDetail;
import com.pranit.todo.repository.UserRepository;
import com.pranit.todo.security.service.GenerateToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final GenerateToken generateToken;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createNewAccount(final RegistrationRequest request) {
        if (this.userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken. Please choose another username.");
        }
        if (this.userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered. Please sign in or use another email.");
        }
        final User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(this.passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();
        this.userRepository.save(user);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public LoginResponse authenticateUser(final LoginRequest request) {
        var authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        final Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetail detail)) {
            throw new BadCredentialsException("Invalid username or password.");
        }
        final TokenResponse response = this.generateToken.generateAccessToken(detail);
        return LoginResponse.builder()
                .username(detail.username())
                .enabled(detail.enabled())
                .accessToken(response.accessToken())
                .tokenType(response.tokenType())
                .expiresIn(response.expiresIn())
                .build();
    }

    @Override
    public AuthenticatedUser getCurrentUser() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetail principal)) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found in security context.");
        }
        final Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return AuthenticatedUser.builder()
                .id(principal.id())
                .username(principal.username())
                .email(principal.email())
                .enabled(principal.enabled())
                .roles(roles)
                .build();
    }
}
