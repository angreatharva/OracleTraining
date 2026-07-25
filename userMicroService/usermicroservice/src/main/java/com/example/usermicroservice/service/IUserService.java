package com.example.usermicroservice.service;

import com.example.usermicroservice.entities.User;

import java.util.List;

public interface IUserService {
    User create(User user);
    User getById(Long id);
    User getByEmail(String email);
    List<User> getAll();
    List<User> getByRoleId(Long roleId);
    List<User> getByManagerId(Long managerId);
    User update(Long id, User user);
    void delete(Long id);
}