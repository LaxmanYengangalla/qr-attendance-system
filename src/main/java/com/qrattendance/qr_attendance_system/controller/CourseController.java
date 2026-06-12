package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import com.qrattendance.qr_attendance_system.repository.StudentCourseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private QRSessionRepository qrSessionRepository;

    // Add course
    @PostMapping
    public Course addCourse(@RequestBody Course course) {
        validateCourse(course);

        course.setCourseName(course.getCourseName().trim());
        course.setGroupName(course.getGroupName().trim());
        course.setLecturerName(course.getLecturerName().trim());

        return courseRepository.save(course);
    }

    @PostMapping("/batch")
    public List<Course> addCoursesBatch(@RequestBody SubjectBatchRequest request) {
        if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Group name is required");
        }

        if (request.getLecturerName() == null || request.getLecturerName().trim().isEmpty()) {
            throw new RuntimeException("Lecturer name is required");
        }

        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new RuntimeException("At least one subject is required");
        }

        List<Course> courses = new ArrayList<>();

        for (String subjectName : request.getSubjects()) {
            if (subjectName == null || subjectName.trim().isEmpty()) {
                continue;
            }

            Course course = new Course();
            course.setCourseName(subjectName.trim());
            course.setGroupName(request.getGroupName().trim());
            course.setLecturerName(request.getLecturerName().trim());
            courses.add(course);
        }

        if (courses.isEmpty()) {
            throw new RuntimeException("At least one subject is required");
        }

        return courseRepository.saveAll(courses);
    }

    private void validateCourse(Course course) {
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new RuntimeException("Subject name is required");
        }

        if (course.getGroupName() == null || course.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Group name is required");
        }

        if (course.getLecturerName() == null || course.getLecturerName().trim().isEmpty()) {
            throw new RuntimeException("Lecturer name is required");
        }
    }

    // Get all courses
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    
    @GetMapping("/teacher/{teacherId}")
    public List<Course> getCoursesByTeacher(@PathVariable Long teacherId) {
        return Collections.emptyList();
    }
    
    @GetMapping("/count")
    public long getCourseCount() {

        return courseRepository.count();
    }

    // Delete course
    @Transactional
    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable Long id) {

        studentCourseRepository.deleteByCourseId(id);
        qrSessionRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);

        return "Course deleted successfully";
    }

    public static class SubjectBatchRequest {
        private String groupName;
        private String lecturerName;
        private List<String> subjects;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getLecturerName() {
            return lecturerName;
        }

        public void setLecturerName(String lecturerName) {
            this.lecturerName = lecturerName;
        }

        public List<String> getSubjects() {
            return subjects;
        }

        public void setSubjects(List<String> subjects) {
            this.subjects = subjects;
        }
    }
 
}
