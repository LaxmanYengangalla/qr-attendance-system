package com.qrattendance.qr_attendance_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.qrattendance.qr_attendance_system.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
	Student findByEmailAndPassword(String email, String password);
	Student findByEmail(String email);
	boolean existsByEmail(String email);
	List<Student> findByApproved(boolean approved);
	Optional<Student> findByRollNo(String rollNo);
	List<Student> findByGroupName(String groupName);
}
