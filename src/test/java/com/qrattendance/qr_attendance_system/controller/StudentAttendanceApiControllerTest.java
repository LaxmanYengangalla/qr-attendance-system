package com.qrattendance.qr_attendance_system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.Attendance;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StudentAttendanceApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private QRSessionRepository qrSessionRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentAttendanceApiController(
                attendanceRepository, courseRepository, qrSessionRepository, studentCourseRepository, studentRepository
        )).build();
    }

    @Test
    void getSessionInfoReturnsCourseDetails() throws Exception {
        QrSession session = session(10L, 3L, LocalDateTime.now().plusMinutes(2));
        Course course = new Course();
        course.setCourseName("Java");
        course.setLecturerName("Prof. Kumar");

        when(qrSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/student/attendance/session/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.courseName").value("Java"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void markAttendanceSuccessfully() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("studentId", 5L);
        QrSession session = session(10L, 3L, LocalDateTime.now().plusMinutes(2));

        when(studentRepository.existsById(5L)).thenReturn(true);
        when(qrSessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.existsByStudentIdAndCourseId(5L, 3L)).thenReturn(true);
        when(attendanceRepository.existsByStudentIdAndQrSession_Id(5L, 10L)).thenReturn(false);

        mockMvc.perform(post("/api/student/attendance/mark")
                        .session(httpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance marked successfully"));

        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void markAttendanceFailsWhenStudentNotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/student/attendance/mark")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please login as student before marking attendance"));

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void markAttendanceRejectsInvalidExpiredDuplicateAndNotEnrolled() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("studentId", 5L);

        when(studentRepository.existsById(5L)).thenReturn(true);
        when(qrSessionRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/student/attendance/mark")
                        .session(httpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":99}"))
                .andExpect(jsonPath("$.message").value("Attendance session not found"));

        QrSession expired = session(10L, 3L, LocalDateTime.now().minusMinutes(1));
        when(qrSessionRepository.findById(10L)).thenReturn(Optional.of(expired));
        mockMvc.perform(post("/api/student/attendance/mark")
                        .session(httpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":10}"))
                .andExpect(jsonPath("$.message").value("QR Code Expired"));

        QrSession active = session(11L, 3L, LocalDateTime.now().plusMinutes(2));
        when(qrSessionRepository.findById(11L)).thenReturn(Optional.of(active));
        when(studentCourseRepository.existsByStudentIdAndCourseId(5L, 3L)).thenReturn(false);
        mockMvc.perform(post("/api/student/attendance/mark")
                        .session(httpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":11}"))
                .andExpect(jsonPath("$.message").value("You are not enrolled in this subject"));

        when(studentCourseRepository.existsByStudentIdAndCourseId(5L, 3L)).thenReturn(true);
        when(attendanceRepository.existsByStudentIdAndQrSession_Id(5L, 11L)).thenReturn(true);
        mockMvc.perform(post("/api/student/attendance/mark")
                        .session(httpSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":11}"))
                .andExpect(jsonPath("$.message").value("Attendance already marked"));
    }

    private QrSession session(Long id, Long courseId, LocalDateTime endTime) {
        QrSession session = new QrSession();
        ReflectionTestUtils.setField(session, "id", id);
        session.setCourseId(courseId);
        session.setStartTime(LocalDateTime.now().minusMinutes(1));
        session.setEndTime(endTime);
        return session;
    }
}
