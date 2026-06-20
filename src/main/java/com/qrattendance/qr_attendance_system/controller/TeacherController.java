package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    // Add teacher (Admin manual addition)
    @PostMapping
    public Teacher addTeacher(@RequestBody Teacher teacher) {
        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (teacher.getEmail() == null || teacher.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (teacher.getSubject() == null || teacher.getSubject().trim().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        if (teacherRepository.existsByEmail(teacher.getEmail().trim())) {
            throw new RuntimeException("Email is already registered");
        }

        teacher.setName(teacher.getName().trim());
        teacher.setEmail(teacher.getEmail().trim());
        teacher.setSubject(teacher.getSubject().trim());
        teacher.setApproved(true);
        teacher.setPassword(passwordService.hash("password123")); // Default password for manual adds

        Teacher saved = teacherRepository.save(teacher);

        // Synchronize with User table so they can log in
        User user = new User();
        user.setName(saved.getName());
        user.setEmail(saved.getEmail());
        user.setPassword(saved.getPassword());
        user.setRole(User.Role.TEACHER);
        userRepository.save(user);

        return saved;
    }

    // Register teacher (New Lecturer Sign Up)
    @PostMapping("/register")
    public Teacher registerTeacher(@RequestBody Teacher teacher) {
        if (teacher.getName() == null || teacher.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (teacher.getEmail() == null || teacher.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (teacher.getPassword() == null || teacher.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (teacher.getSubject() == null || teacher.getSubject().trim().isEmpty()) {
            throw new RuntimeException("Subject is required");
        }

        if (teacherRepository.existsByEmail(teacher.getEmail().trim())) {
            throw new RuntimeException("Email is already registered");
        }

        teacher.setName(teacher.getName().trim());
        teacher.setEmail(teacher.getEmail().trim());
        teacher.setSubject(teacher.getSubject().trim());
        teacher.setPassword(passwordService.hash(teacher.getPassword().trim()));
        teacher.setApproved(false); // Pending approval by admin

        return teacherRepository.save(teacher);
    }

    // Get all approved teachers
    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findByApproved(true);
    }

    // Delete teacher
    @DeleteMapping("/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElse(null);
        if (teacher != null) {
            User user = userRepository.findByEmail(teacher.getEmail());
            if (user != null) {
                userRepository.delete(user);
            }
            teacherRepository.delete(teacher);
        }
        return "Lecturer/Professor deleted successfully";
    }

    // Count approved teachers
    @GetMapping("/count")
    public long getTeacherCount() {
        return teacherRepository.findByApproved(true).size();
    }
}
