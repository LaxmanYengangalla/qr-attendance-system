package com.qrattendance.qr_attendance_system.repository;

import com.qrattendance.qr_attendance_system.model.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface QRSessionRepository extends JpaRepository<QrSession, Long> {
	
	void deleteByCourseId(Long courseId);
}

