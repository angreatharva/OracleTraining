package com.example.usermicroservice.service;

import com.example.usermicroservice.entities.Role;

import java.util.List;

public interface IRoleService {
    Role create(Role role);
    Role getById(Long id);
    Role getByRoleName(String roleName);
    List<Role> getAll();
    Role update(Long id, Role role);
    void delete(Long id);
}