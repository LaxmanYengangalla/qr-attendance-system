package com.qrattendance.qr_attendance_system.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.repository.TeacherRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
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
class TeacherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        TeacherController controller = new TeacherController();
        ReflectionTestUtils.setField(controller, "teacherRepository", teacherRepository);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "passwordService", passwordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void addFindCountAndDeleteTeacher() throws Exception {
        Teacher teacher = teacher("Prof. Kumar", "kumar@example.com", "Java");
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(teacherRepository.findByApproved(true)).thenReturn(List.of(teacher));
        when(passwordService.hash(any(String.class))).thenReturn("sha256$hash");
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(teacher));

        mockMvc.perform(post("/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Prof. Kumar\",\"email\":\"kumar@example.com\",\"subject\":\"Java\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Prof. Kumar"));

        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/teachers/count"))
                .andExpect(jsonPath("$").value(1));

        mockMvc.perform(delete("/teachers/2"))
                .andExpect(status().isOk());

        verify(teacherRepository).delete(teacher);
    }

    private Teacher teacher(String name, String email, String subject) {
        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setEmail(email);
        teacher.setSubject(subject);
        return teacher;
    }
}
