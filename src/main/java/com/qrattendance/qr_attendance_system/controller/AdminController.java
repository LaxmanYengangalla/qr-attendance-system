package com.qrattendance.qr_attendance_system.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

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

    // ✅ Add Teacher
    @PostMapping("/teachers")
    public String addTeacher(@RequestBody Teacher teacher) {
        teacherRepository.save(teacher);
        return "Lecturer/Professor Added Successfully";
    }

    // ✅ Delete Student
    @DeleteMapping("/students/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "Student Deleted Successfully";
    }

    // ✅ Delete Teacher
    @DeleteMapping("/teachers/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        teacherRepository.deleteById(id);
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
}
