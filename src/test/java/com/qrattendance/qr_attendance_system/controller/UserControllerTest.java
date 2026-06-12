package com.qrattendance.qr_attendance_system.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.User;
import com.qrattendance.qr_attendance_system.repository.AttendanceRepository;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
import com.qrattendance.qr_attendance_system.repository.UserRepository;
import com.qrattendance.qr_attendance_system.service.PasswordService;
import com.qrattendance.qr_attendance_system.service.UserService;
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
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private QRSessionRepository qrSessionRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "passwordService", passwordService);
        ReflectionTestUtils.setField(controller, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "studentCourseRepository", studentCourseRepository);
        ReflectionTestUtils.setField(controller, "qrSessionRepository", qrSessionRepository);
        ReflectionTestUtils.setField(controller, "attendanceRepository", attendanceRepository);
        ReflectionTestUtils.setField(controller, "courseRepository", courseRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginSuccessAndFailure() throws Exception {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("sha256$hash");
        user.setRole(User.Role.ADMIN);
        when(userService.getUserByEmail("admin@example.com")).thenReturn(user);
        when(passwordService.matches("secret", "sha256$hash")).thenReturn(true);

        mockMvc.perform(get("/users/login").param("email", "admin@example.com").param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ADMIN")));

        when(passwordService.matches("bad", "sha256$hash")).thenReturn(false);
        mockMvc.perform(get("/users/login").param("email", "admin@example.com").param("password", "bad"))
                .andExpect(content().string("Invalid Email or Password"));
    }

    @Test
    void studentLoginCreatesSession() throws Exception {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 8L);
        student.setEmail("student@example.com");
        student.setPassword("sha256$hash");
        student.setName("Student");
        when(studentRepository.findByEmail("student@example.com")).thenReturn(student);
        when(passwordService.matches("secret", "sha256$hash")).thenReturn(true);

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/users/student/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"));

        assertThat(session.getAttribute("studentId")).isEqualTo(8L);
    }

    @Test
    void userCrudEndpointsDelegate() throws Exception {
        User user = new User();
        user.setName("Admin");
        user.setEmail("admin@example.com");
        user.setPassword("plain");
        user.setRole(User.Role.ADMIN);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(user);
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of(user));
        when(passwordService.hash("plain")).thenReturn("sha256$hash");
        when(userRepository.save(user)).thenReturn(user);

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Admin\",\"email\":\"admin@example.com\",\"password\":\"plain\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users")).andExpect(jsonPath("$[0].email").value("admin@example.com"));
        mockMvc.perform(get("/users/1")).andExpect(jsonPath("$.email").value("admin@example.com"));
        mockMvc.perform(get("/users/email/admin@example.com")).andExpect(jsonPath("$.name").value("Admin"));
        mockMvc.perform(get("/users/role/ADMIN")).andExpect(jsonPath("$[0].email").value("admin@example.com"));
        mockMvc.perform(put("/users/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Admin\",\"email\":\"admin@example.com\",\"password\":\"plain\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        verify(userRepository).save(user);
    }
}
