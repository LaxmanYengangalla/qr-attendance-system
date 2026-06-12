package com.qrattendance.qr_attendance_system.repository;

import com.qrattendance.qr_attendance_system.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	
	void deleteByStudentId(Long studentId);
	@Query(value = """
		    SELECT 
		        course_id,
		        COUNT(*) AS totalClasses,
		        SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount
		    FROM attendance
		    WHERE student_id = :studentId
		    GROUP BY course_id
		    """, nativeQuery = true)

		List<Object[]> getCourseWiseAttendance(Long studentId);
	
	boolean existsByStudentIdAndQrSession_Id(Long studentId, Long qrSessionId);
	
	List<Attendance> findByStudentId(Long studentId);
	List<Attendance> findByCourseId(Long courseId);
	List<Attendance> findByQrSession_Id(Long qrSessionId);
	long countByStatus(String status);
}
