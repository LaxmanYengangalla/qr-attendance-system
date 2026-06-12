package com.qrattendance.qr_attendance_system.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.Attendance;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.model.StudentCourse;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private QRSessionRepository qrSessionRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AttendanceController(
                attendanceRepository, courseRepository, qrSessionRepository, studentCourseRepository
        )).build();
    }

    @Test
    void markAttendanceRequiresStudentRoleAndLogin() throws Exception {
        mockMvc.perform(get("/attendance/mark").param("qrSessionId", "1").param("role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Only students")));

        mockMvc.perform(get("/attendance/mark").param("qrSessionId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please login")));
    }

    @Test
    void markAttendanceSavesPresentRecord() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("studentId", 9L);
        QrSession session = new QrSession();
        session.setCourseId(3L);
        session.setEndTime(LocalDateTime.now().plusMinutes(2));

        when(qrSessionRepository.findById(4L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.existsByStudentIdAndCourseId(9L, 3L)).thenReturn(true);
        when(attendanceRepository.existsByStudentIdAndQrSession_Id(9L, 4L)).thenReturn(false);

        mockMvc.perform(get("/attendance/mark").session(httpSession).param("qrSessionId", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attendance Marked")));

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PRESENT");
    }

    @Test
    void markAttendanceRejectsExpiredDuplicateAndNotEnrolled() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("studentId", 9L);
        QrSession session = new QrSession();
        session.setCourseId(3L);

        session.setEndTime(LocalDateTime.now().plusMinutes(2));
        when(qrSessionRepository.findById(4L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.existsByStudentIdAndCourseId(9L, 3L)).thenReturn(false);
        mockMvc.perform(get("/attendance/mark").session(httpSession).param("qrSessionId", "4"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("not enrolled")));

        when(studentCourseRepository.existsByStudentIdAndCourseId(9L, 3L)).thenReturn(true);
        session.setEndTime(LocalDateTime.now().minusMinutes(1));
        mockMvc.perform(get("/attendance/mark").session(httpSession).param("qrSessionId", "4"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Expired")));

        session.setEndTime(LocalDateTime.now().plusMinutes(2));
        when(attendanceRepository.existsByStudentIdAndQrSession_Id(9L, 4L)).thenReturn(true);
        mockMvc.perform(get("/attendance/mark").session(httpSession).param("qrSessionId", "4"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Already Marked")));
    }

    @Test
    void summariesCountsDeleteAndAbsentMarkingWork() throws Exception {
        Attendance present = attendance(1L, 2L, "PRESENT");
        Attendance absent = attendance(1L, 2L, "ABSENT");
        when(attendanceRepository.findByStudentId(1L)).thenReturn(List.of(present, absent));
        when(attendanceRepository.count()).thenReturn(2L);
        when(attendanceRepository.countByStatus("PRESENT")).thenReturn(1L);
        when(attendanceRepository.countByStatus("ABSENT")).thenReturn(1L);

        mockMvc.perform(get("/attendance/student/1/summary"))
                .andExpect(jsonPath("$.totalClasses").value(2))
                .andExpect(jsonPath("$.present").value(1));
        mockMvc.perform(get("/attendance/count")).andExpect(jsonPath("$").value(2));
        mockMvc.perform(get("/attendance/count/present")).andExpect(jsonPath("$").value(1));
        mockMvc.perform(get("/attendance/percentage")).andExpect(jsonPath("$").value(50.0));
        mockMvc.perform(delete("/attendance/student/1")).andExpect(content().string("Student attendance deleted successfully"));
        verify(attendanceRepository).deleteByStudentId(1L);

        QrSession session = new QrSession();
        session.setCourseId(2L);
        when(qrSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(11L);
        studentCourse.setCourseId(2L);
        when(studentCourseRepository.findByCourseId(2L)).thenReturn(List.of(studentCourse));
        when(attendanceRepository.findByQrSession_Id(5L)).thenReturn(List.of());
        mockMvc.perform(post("/attendance/mark-absent/5")).andExpect(status().isOk());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void courseWiseAttendanceMapsCourseName() throws Exception {
        Course course = new Course();
        course.setCourseName("Java");
        when(attendanceRepository.getCourseWiseAttendance(1L)).thenReturn(List.<Object[]>of(new Object[]{2L, 4L, 3L}));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/attendance/student/1/course-summary"))
                .andExpect(jsonPath("$[0].courseName").value("Java"))
                .andExpect(jsonPath("$[0].percentage").value(75.0));
    }

    private Attendance attendance(Long studentId, Long courseId, String status) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setCourseId(courseId);
        attendance.setStatus(status);
        attendance.setAttendanceTime(LocalDateTime.now());
        return attendance;
    }
}
