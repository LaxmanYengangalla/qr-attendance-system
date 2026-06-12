package com.qrattendance.qr_attendance_system.repository;

import com.qrattendance.qr_attendance_system.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByEmail(String email);
	
	List<User> findByRole(User.Role role);
}
