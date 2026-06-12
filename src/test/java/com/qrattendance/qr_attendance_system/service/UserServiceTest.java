package com.qrattendance.qr_attendance_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    @Test
    void saveUserHashesPasswordAndSaves() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("plain");

        when(passwordService.hash("plain")).thenReturn("sha256$hash");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.saveUser(user);

        assertThat(saved.getPassword()).isEqualTo("sha256$hash");
        verify(passwordService).hash("plain");
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsersDelegatesToRepository() {
        when(userRepository.findAll()).thenReturn(List.of(new User()));

        assertThat(userService.getAllUsers()).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    void getUserByEmailDelegatesToRepository() {
        User user = new User();
        user.setEmail("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(user);

        assertThat(userService.getUserByEmail("admin@example.com")).isSameAs(user);
        verify(userRepository).findByEmail("admin@example.com");
    }
}
