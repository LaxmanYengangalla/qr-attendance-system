package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Attendance;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.model.StudentCourse;// ✅ ADD THIS

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final QRSessionRepository qrSessionRepository;
    private final StudentCourseRepository studentCourseRepository;

    // Constructor Injection
    public AttendanceController(AttendanceRepository attendanceRepository,
                                CourseRepository courseRepository,
                                QRSessionRepository qrSessionRepository,
                                StudentCourseRepository studentCourseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.qrSessionRepository = qrSessionRepository;
        this.studentCourseRepository = studentCourseRepository;
        
        
    }
    
    @GetMapping("/mark")
    public String markAttendance(@RequestParam(required = false) Long studentId,
                                 @RequestParam Long qrSessionId,
                                 @RequestParam(required = false, defaultValue = "STUDENT") String role,
                                 HttpSession httpSession) {
    	
    	// 🔐 ROLE CHECK
    	if (!role.equalsIgnoreCase("STUDENT")) {
    	    return "<h2 style='color:red;'>❌ Only students can mark attendance</h2>";
    	}

        // 🔹 Fetch QR Session
        if (!(httpSession.getAttribute("studentId") instanceof Long loggedInStudentId)) {
            return "<h2 style='color:red;'>Please login as student before marking attendance</h2>";
        }

        studentId = loggedInStudentId;

        QrSession session = qrSessionRepository.findById(qrSessionId)
                .orElseThrow(() -> new RuntimeException("QR Session not found"));

        // 🔹 Course validation
        Long courseId = session.getCourseId();

        boolean isValidStudent = studentCourseRepository
                .existsByStudentIdAndCourseId(studentId, courseId);

        if (!isValidStudent) {
            return "<h2 style='color:red;'>❌ You are not enrolled in this course</h2>";
        }

        // 🔹 QR expiry check
        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            return "<h2 style='color:red;'>❌ QR Code Expired</h2>";
        }

        // 🔹 Duplicate check
        boolean alreadyMarked = attendanceRepository
                .existsByStudentIdAndQrSession_Id(studentId, qrSessionId);

        if (alreadyMarked) {
            return "<h2 style='color:orange;'>⚠ Attendance Already Marked</h2>";
        }

        // 🔹 Save attendance
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setQrSession(session);
        attendance.setCourseId(courseId); // ✅ FIXED (not hardcoded)
        attendance.setAttendanceTime(LocalDateTime.now());
        attendance.setStatus("PRESENT");

        attendanceRepository.save(attendance);

        return "<h2 style='color:green;'>✅ Attendance Marked Successfully</h2>";
    }
    
    @GetMapping("/all")
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
    
    @GetMapping("/student/{studentId}")
    public List<Attendance> getAttendanceByStudent(@PathVariable Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
    
    @GetMapping("/course/{courseId}")
    public List<Attendance> getAttendanceByCourse(@PathVariable Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }
    
    @GetMapping("/student/{studentId}/summary")
    public Map<String, Object> getAttendanceSummary(@PathVariable Long studentId) {

        List<Attendance> records = attendanceRepository.findByStudentId(studentId);

        int totalClasses = records.size();
        int presentCount = (int) records.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();

        double percentage = totalClasses == 0 ? 0 :
                (presentCount * 100.0) / totalClasses;

        Map<String, Object> response = new HashMap<>();
        response.put("totalClasses", totalClasses);
        response.put("present", presentCount);
        response.put("percentage", percentage);

        return response;
    }
    
    @GetMapping("/student/{studentId}/course-summary")
    public List<Map<String, Object>> getCourseWiseAttendance(
            @PathVariable Long studentId) {

        List<Object[]> result =
                attendanceRepository.getCourseWiseAttendance(studentId);

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : result) {

            Long courseId = ((Number) row[0]).longValue();

            int totalClasses = ((Number) row[1]).intValue();

            int presentCount = ((Number) row[2]).intValue();

            double percentage =
                    (presentCount * 100.0) / totalClasses;

            String courseName = courseRepository.findById(courseId)
                    .map(Course::getCourseName)
                    .orElse("Unknown Course");

            Map<String, Object> map = new HashMap<>();

            map.put("courseId", courseId);

            map.put("courseName", courseName);

            map.put("totalClasses", totalClasses);

            map.put("presentCount", presentCount);

            map.put("percentage", percentage);

            response.add(map);
        }

        return response;
    }
    
    @GetMapping("/teacher/course/{courseId}")
    public List<Attendance> getTeacherCourseAttendance(
            @PathVariable Long courseId) {

        return attendanceRepository.findByCourseId(courseId);
    }
    
    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    public Map<String, Object> getCourseAttendancePercentage(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        List<Attendance> records =
                attendanceRepository.findByStudentId(studentId);

        List<Attendance> filtered = records.stream()
                .filter(a -> a.getCourseId().equals(courseId))
                .toList();

        int totalClasses = filtered.size();

        int presentCount = (int) filtered.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();

        double percentage = totalClasses == 0 ? 0 :
                (presentCount * 100.0) / totalClasses;

        Map<String, Object> response = new HashMap<>();

        response.put("studentId", studentId);
        response.put("courseId", courseId);
        response.put("totalClasses", totalClasses);
        response.put("presentCount", presentCount);
        response.put("percentage", percentage);

        return response;
    }
    
    @PostMapping("/mark-absent/{qrSessionId}")
    public String markAbsentStudents(
            @PathVariable Long qrSessionId) {

        // Get QR session
        QrSession session = qrSessionRepository.findById(qrSessionId)
                .orElseThrow(() -> new RuntimeException("QR Session not found"));

        Long courseId = session.getCourseId();

        // Get all enrolled students
        List<StudentCourse> enrolledStudents =
                studentCourseRepository.findByCourseId(courseId);

        // Get all attendance records for this session
        List<Attendance> attendanceList =
                attendanceRepository.findByQrSession_Id(qrSessionId);

        for (StudentCourse sc : enrolledStudents) {

            boolean alreadyMarked = attendanceList.stream()
                    .anyMatch(a ->
                            a.getStudentId().equals(sc.getStudentId())
                    );

            if (!alreadyMarked) {

                Attendance absentAttendance = new Attendance();

                absentAttendance.setStudentId(sc.getStudentId());

                absentAttendance.setCourseId(courseId);

                absentAttendance.setQrSession(session);

                absentAttendance.setAttendanceTime(LocalDateTime.now());

                absentAttendance.setStatus("ABSENT");

                attendanceRepository.save(absentAttendance);
            }
        }

        return "Absent students marked successfully";
    }
    
    @GetMapping("/student/{studentId}/history")
    public List<Attendance> getStudentAttendanceHistory(
            @PathVariable Long studentId) {

        return attendanceRepository.findByStudentId(studentId);
    }
    
    @GetMapping("/teacher/course/{courseId}/history")
    public List<Attendance> getCourseAttendanceHistory(
            @PathVariable Long courseId) {

        return attendanceRepository.findByCourseId(courseId);
    }
    
    @DeleteMapping("/student/{studentId}")
    public String deleteAttendanceByStudent(
            @PathVariable Long studentId) {

        attendanceRepository.deleteByStudentId(studentId);

        return "Student attendance deleted successfully";
    }
    
    @GetMapping("/count")
    public long getAttendanceCount() {

        return attendanceRepository.count();
    }
    
    @GetMapping("/count/present")
    public long getPresentCount() {

        return attendanceRepository.countByStatus("PRESENT");
    }
    
    @GetMapping("/count/absent")
    public long getAbsentCount() {

        return attendanceRepository.countByStatus("ABSENT");
    }
    
    @GetMapping("/percentage")
    public double getOverallAttendancePercentage() {

        long total = attendanceRepository.count();

        long present =
                attendanceRepository.countByStatus("PRESENT");

        if (total == 0) {
            return 0;
        }

        return (present * 100.0) / total;
    }
    
    @GetMapping("/session/{qrSessionId}/present-count")
    public long getPresentCountBySession(
            @PathVariable Long qrSessionId) {

        List<Attendance> attendanceList =
                attendanceRepository.findByQrSession_Id(qrSessionId);

        return attendanceList.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()))
                .count();
    }
    
}
