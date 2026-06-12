package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    // Add teacher
    @PostMapping
    public Teacher addTeacher(@RequestBody Teacher teacher) {

        return teacherRepository.save(teacher);
    }

    // Get all teachers
    @GetMapping
    public List<Teacher> getAllTeachers() {

        return teacherRepository.findAll();
    }

    // Delete teacher
    @DeleteMapping("/{id}")
    public String deleteTeacher(@PathVariable Long id) {

        teacherRepository.deleteById(id);

        return "Lecturer/Professor deleted successfully";
    }

    // Count teachers
    @GetMapping("/count")
    public long getTeacherCount() {

        return teacherRepository.count();
    }
}
