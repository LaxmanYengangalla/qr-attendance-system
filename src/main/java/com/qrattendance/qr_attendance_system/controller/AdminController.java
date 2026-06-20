package com.qrattendance.qr_attendance_system.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    // ✅ Add Student
    @PostMapping("/students")
    public String addStudent(@RequestBody Student student) {
        if (student.getPassword() != null && !student.getPassword().isBlank()) {
            student.setPassword(passwordService.hash(student.getPassword().trim()));
        }
        studentRepository.save(student);
        return "Student Added Successfully";
    }

    // ✅ Add Teacher (Admin manual addition)
    @Transactional
    @PostMapping("/teachers")
    public String addTeacher(@RequestBody Teacher teacher) {
        if (teacherRepository.existsByEmail(teacher.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }
        teacher.setApproved(true);
        teacher.setPassword(passwordService.hash("password123")); // Default password for manual adds
        Teacher saved = teacherRepository.save(teacher);

        // Sync with User table so they can log in
        User user = new User();
        user.setName(saved.getName());
        user.setEmail(saved.getEmail());
        user.setPassword(saved.getPassword());
        user.setRole(User.Role.TEACHER);
        userRepository.save(user);

        return "Lecturer/Professor Added Successfully";
    }

    // ✅ Delete Student
    @DeleteMapping("/students/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "Student Deleted Successfully";
    }

    // ✅ Delete Teacher (Synchronized with User deletion)
    @Transactional
    @DeleteMapping("/teachers/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id).orElse(null);
        if (teacher != null) {
            User user = userRepository.findByEmail(teacher.getEmail());
            if (user != null) {
                userRepository.delete(user);
            }
            teacherRepository.delete(teacher);
        }
        return "Lecturer/Professor Deleted Successfully";
    }
    
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    @GetMapping("/students/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // ✅ List pending lecturer requests
    @GetMapping("/teachers/pending")
    public List<Teacher> getPendingTeachers() {
        return teacherRepository.findByApproved(false);
    }

    // ✅ Approve pending lecturer request
    @Transactional
    @PostMapping("/teachers/{id}/approve")
    public String approveTeacher(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lecturer request not found"));
        teacher.setApproved(true);
        Teacher saved = teacherRepository.save(teacher);

        // Sync with User table so they can log in
        User user = new User();
        user.setName(saved.getName());
        user.setEmail(saved.getEmail());
        user.setPassword(saved.getPassword());
        user.setRole(User.Role.TEACHER);
        userRepository.save(user);

        return "Lecturer Approved Successfully";
    }

    // ✅ Reject/Delete pending lecturer request
    @DeleteMapping("/teachers/pending/{id}")
    public String rejectTeacher(@PathVariable Long id) {
        teacherRepository.deleteById(id);
        return "Lecturer Request Rejected Successfully";
    }

    // ✅ List pending student requests
    @GetMapping("/students/pending")
    public List<Student> getPendingStudents() {
        return studentRepository.findByApproved(false);
    }

    // ✅ Approve pending student request
    @Transactional
    @PostMapping("/students/{id}/approve")
    public String approveStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student request not found"));
        student.setApproved(true);
        studentRepository.save(student);
        return "Student Approved Successfully";
    }

    // ✅ Reject/Delete pending student request
    @DeleteMapping("/students/pending/{id}")
    public String rejectStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "Student Request Rejected Successfully";
    }
}
