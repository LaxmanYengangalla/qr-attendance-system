package com.qrattendance.qr_attendance_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Controller
public class StudentAttendancePageController {

    @GetMapping("/student/attendance/{sessionId}")
    public String attendancePage(@PathVariable Long sessionId, HttpSession session) {
        if (session.getAttribute("studentId") == null) {
            String returnTo = UriUtils.encode("/student/attendance/" + sessionId, StandardCharsets.UTF_8);
            return "redirect:/student.html?returnTo=" + returnTo;
        }

        return "redirect:/student-attendance.html?sessionId=" + sessionId;
    }
}
