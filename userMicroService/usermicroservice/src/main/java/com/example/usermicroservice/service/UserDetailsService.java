package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.CreateUserDetailRequest;
import com.example.usermicroservice.dto.response.UserDetailResponse;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.entities.UserDetail;
import com.example.usermicroservice.enums.KycStatus;
import com.example.usermicroservice.enums.RiskLevel;
import com.example.usermicroservice.exceptions.ResourceNotFoundException;
import com.example.usermicroservice.repositories.UserDetailsRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserDetailsService implements IUserDetailsService {
    private final UserDetailsRepository userDetailRepository;
    private final UserRepository userRepository;

    public UserDetailsService(UserDetailsRepository userDetailRepository, UserRepository userRepository) {
        this.userDetailRepository = userDetailRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailResponse create(CreateUserDetailRequest request) {
        if (userDetailRepository.findByUser_UserId(request.userId()).isPresent()) throw new IllegalStateException("User detail already exists for user: " + request.userId());
        return toResponse(userDetailRepository.save(toEntity(request, new UserDetail())));
    }

    @Override @Transactional(readOnly = true)
    public UserDetailResponse getById(Long id) { return toResponse(getEntityById(id)); }

    @Override @Transactional(readOnly = true)
    public UserDetailResponse getByUserId(Long userId) {
        return toResponse(userDetailRepository.findByUser_UserId(userId).orElseThrow(() -> new ResourceNotFoundException("User detail for user", userId)));
    }

    @Override @Transactional(readOnly = true)
    public List<UserDetailResponse> getAll() { return userDetailRepository.findAll().stream().map(this::toResponse).toList(); }

    @Override
    public UserDetailResponse update(Long id, CreateUserDetailRequest request) {
        UserDetail detail = getEntityById(id);
        if (!detail.getUser().getUserId().equals(request.userId()) && userDetailRepository.findByUser_UserId(request.userId()).isPresent()) {
            throw new IllegalStateException("User detail already exists for user: " + request.userId());
        }
        return toResponse(userDetailRepository.save(toEntity(request, detail)));
    }

    @Override public void delete(Long id) { userDetailRepository.delete(getEntityById(id)); }

    private UserDetail toEntity(CreateUserDetailRequest request, UserDetail detail) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        detail.setUser(user);
        detail.setDateOfBirth(request.dateOfBirth());
        detail.setRiskLevel(request.riskLevel() == null ? null : request.riskLevel().name());
        detail.setRiskScore(request.riskScore());
        detail.setKycStatus(request.kycStatus() == null ? null : request.kycStatus().name());
        return detail;
    }
    private UserDetail getEntityById(Long id) { return userDetailRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User detail", id)); }
    private UserDetailResponse toResponse(UserDetail detail) {
        return new UserDetailResponse(detail.getUserDetailId(), detail.getUser().getUserId(), detail.getDateOfBirth(),
                detail.getRiskLevel() == null ? null : RiskLevel.valueOf(detail.getRiskLevel()), detail.getRiskScore(),
                detail.getKycStatus() == null ? null : KycStatus.valueOf(detail.getKycStatus()));
    }
}
