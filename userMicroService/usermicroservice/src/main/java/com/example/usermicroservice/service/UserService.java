package com.example.usermicroservice.service;

import org.springframework.stereotype.Service;

import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        user.setUserId(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // If you send a UserDetail along with User, keep both sides in sync
        if (user.getUserDetails() != null) {
            user.getUserDetails().setUser(user);
        }

        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getByRoleId(Long roleId) {
        return userRepository.findByRole_RoleId(roleId);
    }

    @Override
    public List<User> getByManagerId(Long managerId) {
        return userRepository.findByManager_UserId(managerId);
    }

    @Override
    public User update(Long id, User user) {
        User existing = getById(id);

        user.setUserId(existing.getUserId());
        user.setCreatedAt(existing.getCreatedAt());
        user.setUpdatedAt(LocalDateTime.now());

        // Preserve relationships if needed, or set them from incoming object
        if (user.getUserDetails() != null) {
            user.getUserDetails().setUser(user);
        }

        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(getById(id));
    }
}