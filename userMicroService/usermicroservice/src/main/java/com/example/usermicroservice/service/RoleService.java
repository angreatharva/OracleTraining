package com.example.usermicroservice.service;

import com.example.usermicroservice.entities.Role;
import com.example.usermicroservice.exceptions.RolesException;
import com.example.usermicroservice.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role create(Role role) {
        role.setRoleId(null);
        return roleRepository.save(role);
    }

    @Override
    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RolesException(String.valueOf(id)));
    }

    @Override
    public Role getByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RolesException(roleName));
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role update(Long id, Role role) {
        Role existing = getById(id);
        role.setRoleId(existing.getRoleId());
        return roleRepository.save(role);
    }

    @Override
    public void delete(Long id) {
        roleRepository.delete(getById(id));
    }
}