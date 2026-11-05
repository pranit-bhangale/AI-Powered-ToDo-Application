package com.pranit.todo.security.service;

import com.pranit.todo.dto.TokenResponse;
import com.pranit.todo.model.UserDetail;

public interface GenerateToken {

    TokenResponse generateAccessToken(UserDetail userDetail);
}