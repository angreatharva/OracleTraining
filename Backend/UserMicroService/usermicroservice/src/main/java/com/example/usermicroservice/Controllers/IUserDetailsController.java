package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserDetailRequest;
import com.example.usermicroservice.dto.response.UserDetailResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IUserDetailsController {
    ResponseEntity<UserDetailResponse> create(CreateUserDetailRequest request);
    List<UserDetailResponse> getAll();
    UserDetailResponse getById(Long id);
    UserDetailResponse getByUserId(Long userId);
    UserDetailResponse update(Long id, CreateUserDetailRequest request);
    void delete(Long id);
}
