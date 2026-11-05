package com.pranit.todo.security.service;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public interface ExtractClaim {

    String getUsernameFromAccessToken(Claims claims);

    UUID getUserIdFromAccessToken(Claims claims);

    Claims validateAndParseToken(String token);
    
    boolean isAccessToken(Claims claims);
}
