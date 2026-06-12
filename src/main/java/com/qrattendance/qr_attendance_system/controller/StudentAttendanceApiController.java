package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Attendance;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StudentAttendanceApiController {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final QRSessionRepository qrSessionRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;

    public StudentAttendanceApiController(AttendanceRepository attendanceRepository,
                                          CourseRepository courseRepository,
                                          QRSessionRepository qrSessionRepository,
                                          StudentCourseRepository studentCourseRepository,
                                          StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.qrSessionRepository = qrSessionRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/api/student/attendance/session/{sessionId}")
    public Map<String, Object> getSessionInfo(@PathVariable Long sessionId) {
        QrSession session = qrSessionRepository.findById(sessionId).orElse(null);
        Map<String, Object> response = new HashMap<>();

        if (session == null) {
            response.put("success", false);
            response.put("message", "Attendance session not found");
            return response;
        }

        Course course = courseRepository.findById(session.getCourseId()).orElse(null);
        response.put("success", true);
        response.put("sessionId", session.getId());
        response.put("courseId", session.getCourseId());
        response.put("courseName", course == null ? "Unknown Subject" : course.getCourseName());
        response.put("lecturerName", course == null ? "Lecturer/Professor" : course.getLecturerName());
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());
        response.put("active", !LocalDateTime.now().isAfter(session.getEndTime()));
        return response;
    }

    @PostMapping("/api/student/attendance/mark")
    public Map<String, Object> markAttendance(@RequestBody MarkAttendanceRequest request,
                                              HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();

        if (request == null || request.getSessionId() == null) {
            return fail("Session ID is required");
        }

        if (!(httpSession.getAttribute("studentId") instanceof Long studentId)) {
            return fail("Please login as student before marking attendance");
        }

        if (!studentRepository.existsById(studentId)) {
            return fail("Student login was not found. Please login again");
        }

        QrSession session = qrSessionRepository.findById(request.getSessionId()).orElse(null);
        if (session == null) {
            return fail("Attendance session not found");
        }

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            return fail("QR Code Expired");
        }

        Long courseId = session.getCourseId();
        boolean enrolled = studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            return fail("You are not enrolled in this subject");
        }

        boolean alreadyMarked = attendanceRepository
                .existsByStudentIdAndQrSession_Id(studentId, request.getSessionId());
        if (alreadyMarked) {
            response.put("success", true);
            response.put("message", "Attendance already marked");
            return response;
        }

        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setQrSession(session);
        attendance.setCourseId(courseId);
        attendance.setAttendanceTime(LocalDateTime.now());
        attendance.setStatus("PRESENT");
        attendanceRepository.save(attendance);

        response.put("success", true);
        response.put("message", "Attendance marked successfully");
        return response;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    public static class MarkAttendanceRequest {
        private Long sessionId;

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }
    }
}
