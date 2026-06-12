package com.qrattendance.qr_attendance_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.qrattendance.qr_attendance_system.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
	Student findByEmailAndPassword(String email, String password);
	Student findByEmail(String email);
	boolean existsByEmail(String email);
}
