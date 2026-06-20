package com.qrattendance.qr_attendance_system.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/qr")
public class QrSessionController {

    private final QRSessionRepository qrSessionRepository;
    private final CourseRepository courseRepository;

    public QrSessionController(QRSessionRepository qrSessionRepository,
                               CourseRepository courseRepository) {
        this.qrSessionRepository = qrSessionRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/start-session")
    public void startSession(@RequestParam Long courseId,
                             @RequestParam String role,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {

        if (!role.equalsIgnoreCase("TEACHER")) {
            response.setContentType("text/html");
            response.getWriter().write("<h2 style='color:red;'>Only lecturers/professors can generate QR</h2>");
            return;
        }

        if (!courseRepository.existsById(courseId)) {
            response.setContentType("text/html");
            response.getWriter().write("<h2 style='color:red;'>Course ID not found. Please enter a valid course ID.</h2>");
            return;
        }

        QrSession session = createSession(courseId, request);
        writeQrImage(session.getQrCodeData(), response);
    }

    @GetMapping("/start-session-info")
    public Map<String, Object> startSessionInfo(@RequestParam Long courseId,
                                                @RequestParam String role,
                                                HttpServletRequest request) {

        if (!role.equalsIgnoreCase("TEACHER")) {
            throw new RuntimeException("Only lecturers/professors can generate QR");
        }

        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course ID not found. Please enter a valid course ID.");
        }

        QrSession session = createSession(courseId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("courseId", session.getCourseId());
        response.put("qrCodeData", session.getQrCodeData());
        response.put("attendanceUrl", session.getQrCodeData());
        response.put("qrImageUrl", "/qr/session/" + session.getId() + "/image");
        response.put("startTime", session.getStartTime());
        response.put("endTime", session.getEndTime());
        response.put("accessMode", detectAccessMode(request));
        response.put("mobileAccessWarning", getMobileAccessWarning(request));

        return response;
    }

    @GetMapping("/session/{sessionId}/image")
    public void sessionQrImage(@PathVariable Long sessionId,
                               HttpServletResponse response) throws Exception {

        QrSession session = qrSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("QR Session not found"));

        writeQrImage(session.getQrCodeData(), response);
    }

    private QrSession createSession(Long courseId, HttpServletRequest request) {
        QrSession session = new QrSession();
        session.setCourseId(courseId);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusMinutes(2));

        qrSessionRepository.save(session);

        session.setQrCodeData(buildStudentQrUrl(request, session.getId(), courseId));

        return qrSessionRepository.save(session);
    }

    private String buildStudentQrUrl(HttpServletRequest request, Long sessionId, Long courseId) {
        return getPublicBaseUrl(request)
                + "/student/attendance/"
                + sessionId;
    }

    private String getPublicBaseUrl(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String scheme = forwardedProto == null || forwardedProto.isBlank()
                ? request.getScheme()
                : forwardedProto.split(",")[0].trim();

        if (forwardedHost != null && !forwardedHost.isBlank()) {
            return scheme + "://" + forwardedHost.split(",")[0].trim();
        }

        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }

    private String detectAccessMode(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }
        host = host.split(",")[0].trim().split(":")[0];

        if ("https".equalsIgnoreCase(scheme)) {
            return "HTTPS";
        }

        if (isLocalhost(host)) {
            return "LOCALHOST";
        }

        if (host.startsWith("192.168.")
                || host.startsWith("10.")
                || host.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")) {
            return "LAN_HTTP";
        }

        return "HTTP";
    }

    private String getMobileAccessWarning(HttpServletRequest request) {
        String accessMode = detectAccessMode(request);
        if ("HTTPS".equals(accessMode) || "LOCALHOST".equals(accessMode)) {
            return "";
        }

        return "Camera scanning requires HTTPS on mobile devices. Use HTTPS or ngrok.";
    }

    private boolean isLocalhost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "0:0:0:0:0:0:0:1".equals(host)
                || "::1".equals(host);
    }

    private void writeQrImage(String qrData, HttpServletResponse response) throws Exception {
        int width = 300;
        int height = 300;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, width, height);

        response.setContentType("image/png");

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
    }
}
