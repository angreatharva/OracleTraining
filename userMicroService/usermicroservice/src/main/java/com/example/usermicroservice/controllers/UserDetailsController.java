package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserDetailRequest;
import com.example.usermicroservice.dto.response.UserDetailResponse;
import com.example.usermicroservice.service.IUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user-details")
public class UserDetailsController implements IUserDetailsController {
    private final IUserDetailsService userDetailService;
    public UserDetailsController(IUserDetailsService userDetailService) { this.userDetailService = userDetailService; }
    @PostMapping @Override public ResponseEntity<UserDetailResponse> create(@Valid @RequestBody CreateUserDetailRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(userDetailService.create(request)); }
    @GetMapping @Override public List<UserDetailResponse> getAll() { return userDetailService.getAll(); }
    @GetMapping("/{id}") @Override public UserDetailResponse getById(@PathVariable Long id) { return userDetailService.getById(id); }
    @GetMapping("/user/{userId}") @Override public UserDetailResponse getByUserId(@PathVariable Long userId) { return userDetailService.getByUserId(userId); }
    @PutMapping("/{id}") @Override public UserDetailResponse update(@PathVariable Long id, @Valid @RequestBody CreateUserDetailRequest request) { return userDetailService.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Override public void delete(@PathVariable Long id) { userDetailService.delete(id); }
}
