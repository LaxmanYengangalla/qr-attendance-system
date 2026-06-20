package com.qrattendance.qr_attendance_system.controller;
// Trigger recompile


import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudentCourseRepository studentCourseRepository;
    @Mock
    private QRSessionRepository qrSessionRepository;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
    }

    @Test
    void addCourseReturnsSavedCourse() throws Exception {
        Course saved = course("Java", "MSC Computer Science", "Dr. Rao");
        when(courseRepository.save(any(Course.class))).thenReturn(saved);

        MockHttpServletRequestBuilder requestBuilder = post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseName\":\"Java\",\"groupName\":\"MSC Computer Science\",\"lecturerName\":\"Dr. Rao\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Java"));

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void addCourseRejectsEmptyGroupName() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseName\":\"Java\",\"groupName\":\" \",\"lecturerName\":\"Dr. Rao\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is5xxServerError());
    }

    @Test
    void addBatchCoursesReturnsSavedCourses() throws Exception {
        CourseController.SubjectBatchRequest request = new CourseController.SubjectBatchRequest();
        request.setGroupName("MSC Computer Science");
        
        CourseController.SubjectEntry e1 = new CourseController.SubjectEntry();
        e1.setCourseName("Java");
        e1.setLecturerName("Dr. Rao");
        
        CourseController.SubjectEntry e2 = new CourseController.SubjectEntry();
        e2.setCourseName("Python");
        e2.setLecturerName("Dr. Rao");
        
        request.setSubjects(List.of(e1, e2));

        when(courseRepository.saveAll(any())).thenReturn(List.of(
                course("Java", "MSC Computer Science", "Dr. Rao"),
                course("Python", "MSC Computer Science", "Dr. Rao")));

        MockHttpServletRequestBuilder requestBuilder = post("/courses/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"MSC Computer Science\",\"subjects\":[{\"courseName\":\"Java\",\"lecturerName\":\"Dr. Rao\"},{\"courseName\":\"Python\",\"lecturerName\":\"Dr. Rao\"}]}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addBatchCoursesRejectsEmptySubjectListAndLecturer() throws Exception {
        CourseController.SubjectBatchRequest request = new CourseController.SubjectBatchRequest();
        request.setGroupName("MSC Computer Science");
        request.setSubjects(List.of());

        MockHttpServletRequestBuilder requestBuilder = post("/courses/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"MSC Computer Science\",\"subjects\":[]}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateCourseReturnsUpdatedCourse() throws Exception {
        Course existing = course("Java", "MSC Computer Science", "Dr. Rao");
        existing.setId(4L);

        Course updated = course("Java", "MSC Computer Science", "Dr. Prasad");
        updated.setId(4L);

        when(courseRepository.findById(4L)).thenReturn(java.util.Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenReturn(updated);

        MockHttpServletRequestBuilder requestBuilder = put("/courses/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lecturerName\":\"Dr. Prasad\"}");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lecturerName").value("Dr. Prasad"));
    }

    @Test
    void getAllAndDeleteCourse() throws Exception {
        when(courseRepository.findAll()).thenReturn(List.of(course("Java", "MSC", "Dr. Rao")));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/courses/4"))
                .andExpect(status().isOk());

        verify(studentCourseRepository).deleteByCourseId(4L);
        verify(qrSessionRepository).deleteByCourseId(4L);
        verify(courseRepository).deleteById(4L);
    }

    private Course course(String name, String group, String lecturer) {
        Course course = new Course();
        course.setCourseName(name);
        course.setGroupName(group);
        course.setLecturerName(lecturer);
        return course;
    }
}
