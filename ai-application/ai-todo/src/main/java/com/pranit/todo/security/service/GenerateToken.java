package com.pranit.todo.security.service;

import com.pranit.todo.model.UserDetail;

public interface GenerateToken {

    String generateAccessToken(UserDetail userDetail);
}