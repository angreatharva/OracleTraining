package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.ChangePasswordRequest;
import com.example.usermicroservice.dto.request.LoginRequest;
import com.example.usermicroservice.dto.response.LoginResponse;
import com.example.usermicroservice.dto.response.UserResponse;

public interface IAuthService {

    LoginResponse login(LoginRequest request);

    UserResponse me(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);
}
