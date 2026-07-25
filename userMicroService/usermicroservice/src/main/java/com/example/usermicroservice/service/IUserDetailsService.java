package com.example.usermicroservice.service;
import com.example.usermicroservice.entities.UserDetail;

import java.util.List;

public interface IUserDetailsService {
    UserDetail create(UserDetail userDetail);
    UserDetail getById(Long id);
    UserDetail getByUserId(Long userId);
    List<UserDetail> getAll();
    UserDetail update(Long id, UserDetail userDetail);
    void delete(Long id);
}