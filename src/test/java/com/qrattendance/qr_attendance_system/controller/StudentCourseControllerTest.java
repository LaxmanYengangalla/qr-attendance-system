package com.qrattendance.qr_attendance_system.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.qrattendance.qr_attendance_system.model.StudentCourse;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StudentCourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        StudentCourseController controller = new StudentCourseController();
        ReflectionTestUtils.setField(controller, "studentCourseRepository", studentCourseRepository);
        ReflectionTestUtils.setField(controller, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(controller, "courseRepository", courseRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void enrollStudentValidatesAndSaves() throws Exception {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(studentCourseRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);

        mockMvc.perform(post("/student-courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"courseId\":2}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Enrollment successful"));
    }

    @Test
    void enrollStudentHandlesMissingAndDuplicateCases() throws Exception {
        mockMvc.perform(post("/student-courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"courseId\":2}"))
                .andExpect(content().string("Student not found"));

        when(studentRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(post("/student-courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"courseId\":2}"))
                .andExpect(content().string("Course not found"));

        when(courseRepository.existsById(2L)).thenReturn(true);
        when(studentCourseRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(true);
        mockMvc.perform(post("/student-courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":1,\"courseId\":2}"))
                .andExpect(content().string("Student already enrolled"));
    }

    @Test
    void queryAndRemoveEnrollmentEndpoints() throws Exception {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(1L);
        studentCourse.setCourseId(2L);
        when(studentCourseRepository.findAll()).thenReturn(List.of(studentCourse));
        when(studentCourseRepository.findByStudentId(1L)).thenReturn(List.of(studentCourse));
        when(studentCourseRepository.findByCourseId(2L)).thenReturn(List.of(studentCourse));

        mockMvc.perform(get("/student-courses")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/student-courses/student/1")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/student-courses/course/2")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(delete("/student-courses/remove").param("studentId", "1").param("courseId", "2"))
                .andExpect(content().string("Enrollment removed successfully"));

        verify(studentCourseRepository).deleteByStudentIdAndCourseId(1L, 2L);
    }
}
