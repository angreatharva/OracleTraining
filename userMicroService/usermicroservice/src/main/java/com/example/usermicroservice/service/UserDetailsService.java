package com.example.usermicroservice.service;

import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.entities.UserDetail;
import com.example.usermicroservice.exceptions.UserDetailsException;
import com.example.usermicroservice.exceptions.UserException;
import com.example.usermicroservice.repositories.UserDetailsRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserDetailsService implements IUserDetailsService {

    private final UserDetailsRepository userDetailRepository;
    private final UserRepository userRepository;

    public UserDetailsService(UserDetailsRepository userDetailRepository, UserRepository userRepository) {
        this.userDetailRepository = userDetailRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetail create(UserDetail userDetail) {
        userDetail.setUserDetailId(null);
        userDetail.setCreatedAt(LocalDateTime.now());
        userDetail.setUpdatedAt(LocalDateTime.now());

        if (userDetail.getUser() == null || userDetail.getUser().getUserId() == null) {
            throw new UserException("User is required for UserDetail");
        }

        User user = userRepository.findById(userDetail.getUser().getUserId())
                .orElseThrow(() -> new UserException(String.valueOf(userDetail.getUser().getUserId())));

        userDetail.setUser(user);
        user.setUserDetails(userDetail);

        return userDetailRepository.save(userDetail);
    }

    @Override
    public UserDetail getById(Long id) {
        return userDetailRepository.findById(id)
                .orElseThrow(() -> new UserDetailsException(String.valueOf(id)));
    }

    @Override
    public UserDetail getByUserId(Long userId) {
        return userDetailRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new UserDetailsException("UserDetail not found for userId: " + userId));
    }

    @Override
    public List<UserDetail> getAll() {
        return userDetailRepository.findAll();
    }

    @Override
    public UserDetail update(Long id, UserDetail userDetail) {
        UserDetail existing = getById(id);

        userDetail.setUserDetailId(existing.getUserDetailId());
        userDetail.setCreatedAt(existing.getCreatedAt());
        userDetail.setUpdatedAt(LocalDateTime.now());

        if (userDetail.getUser() != null && userDetail.getUser().getUserId() != null) {
            User user = userRepository.findById(userDetail.getUser().getUserId())
                    .orElseThrow(() -> new UserException(String.valueOf(userDetail.getUser().getUserId())));
            userDetail.setUser(user);
            user.setUserDetails(userDetail);
        }

        return userDetailRepository.save(userDetail);
    }

    @Override
    public void delete(Long id) {
        userDetailRepository.delete(getById(id));
    }
}