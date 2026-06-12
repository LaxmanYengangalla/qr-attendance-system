package com.qrattendance.qr_attendance_system.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(
                userRepository, courseRepository, attendanceRepository, studentRepository, teacherRepository
        )).build();
    }

    @Test
    void getDashboardStatsReturnsCounts() throws Exception {
        when(userRepository.count()).thenReturn(2L);
        when(studentRepository.count()).thenReturn(5L);
        when(teacherRepository.count()).thenReturn(3L);
        when(courseRepository.count()).thenReturn(4L);
        when(attendanceRepository.count()).thenReturn(9L);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.totalStudents").value(5))
                .andExpect(jsonPath("$.totalTeachers").value(3))
                .andExpect(jsonPath("$.totalCourses").value(4))
                .andExpect(jsonPath("$.totalAttendance").value(9));
    }
}
