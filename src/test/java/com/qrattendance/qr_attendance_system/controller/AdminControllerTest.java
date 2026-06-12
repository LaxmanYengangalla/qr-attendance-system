package com.qrattendance.qr_attendance_system.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;
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
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        AdminController controller = new AdminController();
        ReflectionTestUtils.setField(controller, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(controller, "teacherRepository", teacherRepository);
        ReflectionTestUtils.setField(controller, "passwordService", passwordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void studentAndTeacherAdminEndpointsWork() throws Exception {
        Student student = new Student();
        student.setName("Student");
        student.setEmail("student@example.com");
        when(passwordService.hash("secret")).thenReturn("sha256$hash");
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        mockMvc.perform(post("/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Student\",\"email\":\"student@example.com\",\"password\":\"secret\"}"))
                .andExpect(content().string("Student Added Successfully"));
        mockMvc.perform(get("/admin/students")).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/admin/students/1")).andExpect(jsonPath("$.email").value("student@example.com"));
        mockMvc.perform(delete("/admin/students/1")).andExpect(content().string("Student Deleted Successfully"));
        verify(studentRepository).save(any(Student.class));

        mockMvc.perform(post("/admin/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Prof\",\"email\":\"prof@example.com\",\"subject\":\"Java\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Lecturer/Professor Added Successfully"));
        mockMvc.perform(delete("/admin/teachers/2")).andExpect(content().string("Lecturer/Professor Deleted Successfully"));
        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherRepository).deleteById(2L);
    }
}
