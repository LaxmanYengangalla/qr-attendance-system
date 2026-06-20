package com.qrattendance.qr_attendance_system.repository;

import com.qrattendance.qr_attendance_system.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByGroupName(String groupName);
}
