package com.qrattendance.qr_attendance_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.qrattendance.qr_attendance_system.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}