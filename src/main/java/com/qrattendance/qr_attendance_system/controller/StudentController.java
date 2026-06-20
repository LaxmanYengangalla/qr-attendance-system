package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final PasswordService passwordService;

    public StudentController(StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             StudentCourseRepository studentCourseRepository,
                             CourseRepository courseRepository,
                             PasswordService passwordService) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.passwordService = passwordService;
    }

    // Add student
    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new RuntimeException("Student name is required");
        }

        if (student.getRollNo() == null || student.getRollNo().trim().isEmpty()) {
            throw new RuntimeException("Roll number is required");
        }

        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (student.getGroupName() == null || student.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Group is required");
        }

        if (studentRepository.existsByEmail(student.getEmail().trim())) {
            throw new RuntimeException("Email is already registered");
        }

        if (student.getCourseId() != null && !courseRepository.existsById(student.getCourseId())) {
            throw new RuntimeException("Selected subject was not found");
        }

        student.setName(student.getName().trim());
        student.setRollNo(student.getRollNo().trim());
        student.setEmail(student.getEmail().trim());
        student.setGroupName(student.getGroupName().trim());

        if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
            student.setPassword(student.getRollNo());
        }
        student.setPassword(passwordService.hash(student.getPassword().trim()));
        student.setApproved(true); // Manually added by admin is auto-approved

        return studentRepository.save(student);
    }

    // Register student (New Student Sign Up)
    @PostMapping("/register")
    public Student registerStudent(@RequestBody Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new RuntimeException("Student name is required");
        }
        if (student.getRollNo() == null || student.getRollNo().trim().isEmpty()) {
            throw new RuntimeException("Roll number is required");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (student.getGroupName() == null || student.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Group is required");
        }
        if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (studentRepository.existsByEmail(student.getEmail().trim())) {
            throw new RuntimeException("Email is already registered");
        }

        student.setName(student.getName().trim());
        student.setRollNo(student.getRollNo().trim());
        student.setEmail(student.getEmail().trim());
        student.setGroupName(student.getGroupName().trim());
        student.setPassword(passwordService.hash(student.getPassword().trim()));
        student.setApproved(false); // Pending approval by admin

        return studentRepository.save(student);
    }

    // Get all students (Approved only)
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findByApproved(true);
    }

    // Get student by roll number
    @GetMapping("/roll/{rollNo}")
    public Student getStudentByRollNo(@PathVariable String rollNo) {
        return studentRepository.findByRollNo(rollNo.trim())
                .orElseThrow(() -> new RuntimeException("Student not found with roll number: " + rollNo));
    }

    // Delete student
    @Transactional
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {

        attendanceRepository.deleteByStudentId(id);
        studentCourseRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);

        return "Student deleted successfully";
    }

    // Total students count
    @GetMapping("/count")
    public long getStudentCount() {
        return studentRepository.findByApproved(true).size();
    }
}
