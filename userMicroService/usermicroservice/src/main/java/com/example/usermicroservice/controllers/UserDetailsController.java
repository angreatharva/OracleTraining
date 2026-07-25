package com.example.usermicroservice.controllers;

import com.example.usermicroservice.entities.UserDetail;
import com.example.usermicroservice.service.IUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-details")
public class UserDetailsController {

    private final IUserDetailsService userDetailService;

    public UserDetailsController(IUserDetailsService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping
    public ResponseEntity<UserDetail> create(@RequestBody UserDetail userDetail) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userDetailService.create(userDetail));
    }

    @GetMapping
    public List<UserDetail> getAll() {
        return userDetailService.getAll();
    }

    @GetMapping("/{id}")
    public UserDetail getById(@PathVariable Long id) {
        return userDetailService.getById(id);
    }

    @GetMapping("/user/{userId}")
    public UserDetail getByUserId(@PathVariable Long userId) {
        return userDetailService.getByUserId(userId);
    }

    @PutMapping("/{id}")
    public UserDetail update(@PathVariable Long id, @RequestBody UserDetail userDetail) {
        return userDetailService.update(id, userDetail);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userDetailService.delete(id);
    }
}