package com.pranit.todo.security.context;

import com.pranit.todo.exception.UnauthorizedException;
import com.pranit.todo.model.UserDetail;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@Slf4j
@UtilityClass
public class SecurityContext {

    public static UUID getCurrentUserId() {
        final UUID userId = getUserDetail().id();
        if (userId == null) {
            throw new UnauthorizedException("Authenticated user ID not found in security context.");
        }
        return userId;
    }

    public static UserDetail getUserDetail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            log.warn("User is not authenticated: {}", authentication);
            throw new UnauthorizedException("User is not authenticated. Please provide a valid Bearer token.");
        }
        final Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetail userDetail)) {
            log.warn("Invalid authentication principal: {}", principal);
            throw new UnauthorizedException("Invalid authentication principal. Expected UserDetail instance.");
        }
        return userDetail;
    }

    public static String getCurrentUserName() {
        return getUserDetail().getUsername();
    }
}