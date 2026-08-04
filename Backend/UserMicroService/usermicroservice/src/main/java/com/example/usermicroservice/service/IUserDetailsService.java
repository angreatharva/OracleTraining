package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.CreateUserDetailRequest;
import com.example.usermicroservice.dto.response.UserDetailResponse;

import java.util.List;

public interface IUserDetailsService {
    UserDetailResponse create(CreateUserDetailRequest request);
    UserDetailResponse getById(Long id);
    UserDetailResponse getByUserId(Long userId);
    List<UserDetailResponse> getAll();
    UserDetailResponse update(Long id, CreateUserDetailRequest request);
    void delete(Long id);
}
