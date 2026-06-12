package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.StudentCourse;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/student-courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Enroll student into course
    @PostMapping("/enroll")
    public String enrollStudent(@RequestBody StudentCourse studentCourse) {

        // Check student exists
        boolean studentExists =
                studentRepository.existsById(studentCourse.getStudentId());

        if (!studentExists) {
            return "Student not found";
        }

        // Check course exists
        boolean courseExists =
                courseRepository.existsById(studentCourse.getCourseId());

        if (!courseExists) {
            return "Course not found";
        }

        // Check already enrolled
        boolean alreadyEnrolled =
                studentCourseRepository.existsByStudentIdAndCourseId(
                        studentCourse.getStudentId(),
                        studentCourse.getCourseId()
                );

        if (alreadyEnrolled) {
            return "Student already enrolled";
        }

        // Save enrollment
        studentCourseRepository.save(studentCourse);

        return "Enrollment successful";
        
    }
    
    @GetMapping
    public List<StudentCourse> getAllEnrollments() {
        return studentCourseRepository.findAll();
    }
    @GetMapping("/student/{studentId}")
    public List<StudentCourse> getCoursesByStudent(
            @PathVariable Long studentId) {

        return studentCourseRepository.findByStudentId(studentId);
    }
    
    @GetMapping("/course/{courseId}")
    public List<StudentCourse> getStudentsByCourse(
            @PathVariable Long courseId) {

        return studentCourseRepository.findByCourseId(courseId);
    }
    @DeleteMapping("/remove")
    public String removeEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {

        studentCourseRepository
                .deleteByStudentIdAndCourseId(studentId, courseId);

        return "Enrollment removed successfully";
    }
}