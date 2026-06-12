package com.qrattendance.qr_attendance_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.qrattendance.qr_attendance_system.model.StudentCourse;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    void deleteByCourseId(Long courseId);

    void deleteByStudentId(Long studentId);
    
    @Transactional
    @Modifying
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<StudentCourse> findByStudentId(Long studentId);

    List<StudentCourse> findByCourseId(Long courseId);
}
