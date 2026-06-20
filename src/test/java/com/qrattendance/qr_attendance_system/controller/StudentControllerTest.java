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

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(
                studentRepository, attendanceRepository, studentCourseRepository, courseRepository, passwordService
        )).build();
    }

    @Test
    void addStudentHashesPasswordAndSaves() throws Exception {
        Student student = student("Kiran", "R1", "kiran@example.com", "MSC Computer Science", "secret");
        when(studentRepository.existsByEmail("kiran@example.com")).thenReturn(false);
        when(passwordService.hash("secret")).thenReturn("sha256$hash");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kiran\",\"rollNo\":\"R1\",\"email\":\"kiran@example.com\",\"groupName\":\"MSC Computer Science\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("sha256$hash"));

        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void addStudentRejectsValidationFailuresAndDuplicateEmail() throws Exception {
        Student missingName = student("", "R1", "a@example.com", "MSC", "secret");
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"rollNo\":\"R1\",\"email\":\"a@example.com\",\"groupName\":\"MSC\",\"password\":\"secret\"}"))
                .andExpect(status().is5xxServerError());

        Student duplicate = student("Kiran", "R1", "a@example.com", "MSC", "secret");
        when(studentRepository.existsByEmail("a@example.com")).thenReturn(true);
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kiran\",\"rollNo\":\"R1\",\"email\":\"a@example.com\",\"groupName\":\"MSC\",\"password\":\"secret\"}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllCountAndDeleteStudent() throws Exception {
        when(studentRepository.findByApproved(true)).thenReturn(List.of(student("A", "1", "a@example.com", "MSC", "p")));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/students/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        mockMvc.perform(delete("/students/9"))
                .andExpect(status().isOk());

        verify(attendanceRepository).deleteByStudentId(9L);
        verify(studentCourseRepository).deleteByStudentId(9L);
        verify(studentRepository).deleteById(9L);
    }

    private Student student(String name, String roll, String email, String group, String password) {
        Student student = new Student();
        student.setName(name);
        student.setRollNo(roll);
        student.setEmail(email);
        student.setGroupName(group);
        student.setPassword(password);
        return student;
    }
}
