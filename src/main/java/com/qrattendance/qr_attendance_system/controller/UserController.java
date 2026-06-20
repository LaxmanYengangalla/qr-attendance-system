package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.service.PasswordService;
import com.qrattendance.qr_attendance_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    // Add user
    
    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private QRSessionRepository qrSessionRepository;
    
   
    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public Object getUserById(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElse(null);

        if (user == null) {
            return "User not found";
        }

        return user;
    }
    
    
    @GetMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password) {

        User user = userService.getUserByEmail(email);

        if (user == null || !passwordService.matches(password, user.getPassword())) {
            return "Invalid Email or Password";
        }

        return "Login Successful - Role: " + user.getRole();
    }
    
    
    @PostMapping("/student/login")
    public Student studentLogin(@RequestBody Student loginRequest,
                                HttpSession session) {

        Student student = studentRepository.findByEmail(loginRequest.getEmail());

        if (student != null) {
            if (!student.isApproved()) {
                throw new RuntimeException("Access pending Admin approval");
            }
            if (!passwordService.matches(loginRequest.getPassword(), student.getPassword())) {
                student = null;
            }
        }

        if (student != null) {
            session.setAttribute("studentId", student.getId());
            session.setAttribute("studentName", student.getName());
            session.setAttribute("studentEmail", student.getEmail());
        }

        return student;
    }

    @GetMapping("/student/session")
    public Object getStudentSession(HttpSession session) {
        Object studentId = session.getAttribute("studentId");

        if (studentId == null) {
            return null;
        }

        return studentRepository.findById((Long) studentId).orElse(null);
    }

    @PostMapping("/student/logout")
    public String studentLogout(HttpSession session) {
        session.invalidate();
        return "Student logged out";
    }
    
    @Transactional
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        // Delete attendance records
        attendanceRepository.deleteByStudentId(id);

        // Delete ser
        userRepository.deleteById(id);

        return "User deleted successfully";
    }
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User updatedUser) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPassword(passwordService.hash(updatedUser.getPassword()));
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }
    
    @GetMapping("/email/{email}")
    public Object getUserByEmail(@PathVariable String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return "User not found";
        }

        return user;
    }
    
    @GetMapping("/role/{role}")
    public List<User> getUsersByRole(@PathVariable User.Role role) {

        return userRepository.findByRole(role);
    }
    
    
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;
}
