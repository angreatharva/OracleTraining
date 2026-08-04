package com.example.usermicroservice.service;

import com.example.usermicroservice.clients.BankRecordClient;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.exceptions.UserDeletionBlockedException;
import com.example.usermicroservice.repositories.RoleRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private BankRecordClient bankRecordClient;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, bankRecordClient, passwordEncoder);
    }

    @Test
    void deleteIsBlockedWhenBankHasRecordsForUser() {
        User user = user(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(bankRecordClient.hasBankAccountOrKycDocument(10L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(10L))
                .isInstanceOf(UserDeletionBlockedException.class);

        verify(userRepository, never()).delete(user);
    }

    @Test
    void deleteIsAllowedWhenBankHasNoRecordsForUser() {
        User user = user(11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(bankRecordClient.hasBankAccountOrKycDocument(11L)).thenReturn(false);

        userService.delete(11L);

        verify(userRepository).delete(user);
    }

    private User user(Long userId) {
        return User.builder().userId(userId).subordinates(new ArrayList<>()).build();
    }
}
