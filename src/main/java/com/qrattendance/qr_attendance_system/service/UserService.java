package com.qrattendance.qr_attendance_system.service;

import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    // Save user
    public User saveUser(User user) {
        user.setPassword(passwordService.hash(user.getPassword()));
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
