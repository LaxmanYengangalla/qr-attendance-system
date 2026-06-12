package com.qrattendance.qr_attendance_system.controller;
import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final UserRepository userRepository;

    private final CourseRepository courseRepository;

    private final AttendanceRepository attendanceRepository;

    private final StudentRepository studentRepository;

    private final TeacherRepository teacherRepository;

    public DashboardController(
            UserRepository userRepository,
            CourseRepository courseRepository,
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository) {

        this.userRepository = userRepository;

        this.courseRepository = courseRepository;

        this.attendanceRepository = attendanceRepository;

        this.studentRepository = studentRepository;

        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {

        long totalUsers =
                userRepository.count();

        long totalStudents = studentRepository.count();

        long totalTeachers = teacherRepository.count();

        long totalCourses =
                courseRepository.count();

        long totalAttendance =
                attendanceRepository.count();

        Map<String, Object> response =
                new HashMap<>();

        response.put("totalUsers", totalUsers);

        response.put("totalStudents", totalStudents);

        response.put("totalTeachers", totalTeachers);

        response.put("totalCourses", totalCourses);

        response.put("totalAttendance", totalAttendance);

        return response;
    }
}
