package com.qrattendance.qr_attendance_system.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class QrSessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QRSessionRepository qrSessionRepository;
    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new QrSessionController(qrSessionRepository, courseRepository)).build();
    }

    @Test
    void startSessionInfoCreatesQrSession() throws Exception {
        when(courseRepository.existsById(3L)).thenReturn(true);
        when(qrSessionRepository.save(any(QrSession.class))).thenAnswer(invocation -> {
            QrSession session = invocation.getArgument(0);
            if (session.getId() == null) {
                ReflectionTestUtils.setField(session, "id", 50L);
            }
            return session;
        });

        mockMvc.perform(get("/qr/start-session-info")
                        .param("courseId", "3")
                        .param("role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(50))
                .andExpect(jsonPath("$.qrCodeData", containsString("/student/attendance/50")));

        verify(qrSessionRepository).save(any(QrSession.class));
    }

    @Test
    void startSessionRejectsUnauthorizedRoleAndMissingCourse() throws Exception {
        mockMvc.perform(get("/qr/start-session").param("courseId", "3").param("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Only lecturers")));

        when(courseRepository.existsById(99L)).thenReturn(false);
        mockMvc.perform(get("/qr/start-session-info").param("courseId", "99").param("role", "TEACHER"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void sessionQrImageReturnsPng() throws Exception {
        QrSession session = new QrSession();
        session.setQrCodeData("http://localhost/student/attendance/10");
        when(qrSessionRepository.findById(10L)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/qr/session/10/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }
}
