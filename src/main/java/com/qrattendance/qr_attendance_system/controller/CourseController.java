package com.qrattendance.qr_attendance_system.controller;

import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.repository.CourseRepository;
import com.qrattendance.qr_attendance_system.repository.StudentRepository;
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
    private StudentRepository studentRepository;

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

        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new RuntimeException("At least one subject is required");
        }

        List<Course> courses = new ArrayList<>();

        for (SubjectEntry entry : request.getSubjects()) {
            if (entry.getCourseName() == null || entry.getCourseName().trim().isEmpty()) {
                continue;
            }
            if (entry.getLecturerName() == null || entry.getLecturerName().trim().isEmpty()) {
                throw new RuntimeException("Lecturer name is required for subject " + entry.getCourseName());
            }

            Course course = new Course();
            course.setCourseName(entry.getCourseName().trim());
            course.setGroupName(request.getGroupName().trim());
            course.setLecturerName(entry.getLecturerName().trim());
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

    // Rename group name across courses and students
    @Transactional
    @PutMapping("/group/rename")
    public String renameGroup(@RequestParam String oldGroupName, @RequestParam String newGroupName) {
        if (oldGroupName == null || oldGroupName.trim().isEmpty() || newGroupName == null || newGroupName.trim().isEmpty()) {
            throw new RuntimeException("Old and new group names are required");
        }

        String oldGroup = oldGroupName.trim();
        String newGroup = newGroupName.trim();

        // 1. Update Courses
        List<Course> courses = courseRepository.findByGroupName(oldGroup);
        for (Course course : courses) {
            course.setGroupName(newGroup);
        }
        courseRepository.saveAll(courses);

        // 2. Update Students
        List<Student> students = studentRepository.findByGroupName(oldGroup);
        for (Student student : students) {
            student.setGroupName(newGroup);
        }
        studentRepository.saveAll(students);

        return "Group renamed successfully from " + oldGroup + " to " + newGroup;
    }

    // Update course (e.g. lecturer name)
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (courseDetails.getCourseName() != null && !courseDetails.getCourseName().trim().isEmpty()) {
            course.setCourseName(courseDetails.getCourseName().trim());
        }

        if (courseDetails.getLecturerName() != null && !courseDetails.getLecturerName().trim().isEmpty()) {
            course.setLecturerName(courseDetails.getLecturerName().trim());
        }

        if (courseDetails.getGroupName() != null && !courseDetails.getGroupName().trim().isEmpty()) {
            course.setGroupName(courseDetails.getGroupName().trim());
        }

        return courseRepository.save(course);
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
        private List<SubjectEntry> subjects;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<SubjectEntry> getSubjects() {
            return subjects;
        }

        public void setSubjects(List<SubjectEntry> subjects) {
            this.subjects = subjects;
        }
    }

    public static class SubjectEntry {
        private String courseName;
        private String lecturerName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getLecturerName() {
            return lecturerName;
        }

        public void setLecturerName(String lecturerName) {
            this.lecturerName = lecturerName;
        }
    }
 
}
