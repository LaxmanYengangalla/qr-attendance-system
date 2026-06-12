package com.qrattendance.qr_attendance_system.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import com.qrattendance.qr_attendance_system.model.Attendance;
import com.qrattendance.qr_attendance_system.model.Course;
import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.model.Student;
import com.qrattendance.qr_attendance_system.model.StudentCourse;
import com.qrattendance.qr_attendance_system.model.Teacher;
import com.qrattendance.qr_attendance_system.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "run.jpa.tests", matches = "true")
class RepositoryIntegrationTest {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private QRSessionRepository qrSessionRepository;
    @Autowired
    private StudentCourseRepository studentCourseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void studentRepositoryFindsByEmailAndPasswordAndEmail() {
        Student student = new Student();
        student.setName("Asha");
        student.setEmail("asha@example.com");
        student.setPassword("secret");
        student.setRollNo("R1");
        student.setGroupName("MSC Computer Science");
        studentRepository.save(student);

        assertThat(studentRepository.findByEmailAndPassword("asha@example.com", "secret")).isNotNull();
        assertThat(studentRepository.findByEmail("asha@example.com")).isNotNull();
        assertThat(studentRepository.existsByEmail("asha@example.com")).isTrue();
    }

    @Test
    void attendanceRepositoryCustomQueriesWork() {
        Course course = new Course();
        course.setCourseName("Java");
        course.setGroupName("MSC");
        course.setLecturerName("Prof");
        Course savedCourse = courseRepository.save(course);

        QrSession session = new QrSession();
        session.setCourseId(savedCourse.getId());
        session.setQrCodeData("QR");
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusMinutes(2));
        QrSession savedSession = qrSessionRepository.save(session);

        Attendance attendance = new Attendance();
        attendance.setStudentId(1L);
        attendance.setCourseId(savedCourse.getId());
        attendance.setQrSession(savedSession);
        attendance.setStatus("PRESENT");
        attendance.setAttendanceTime(LocalDateTime.now());
        attendanceRepository.save(attendance);

        assertThat(attendanceRepository.existsByStudentIdAndQrSession_Id(1L, savedSession.getId())).isTrue();
        assertThat(attendanceRepository.findByStudentId(1L)).hasSize(1);
        assertThat(attendanceRepository.findByCourseId(savedCourse.getId())).hasSize(1);
        assertThat(attendanceRepository.findByQrSession_Id(savedSession.getId())).hasSize(1);
        assertThat(attendanceRepository.countByStatus("PRESENT")).isEqualTo(1);

        List<Object[]> summary = attendanceRepository.getCourseWiseAttendance(1L);
        assertThat(summary).hasSize(1);
        assertThat(((Number) summary.get(0)[2]).intValue()).isEqualTo(1);
    }

    @Test
    void studentCourseTeacherUserAndQrRepositoriesSupportCrudAndCustomMethods() {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(1L);
        studentCourse.setCourseId(2L);
        studentCourseRepository.save(studentCourse);

        assertThat(studentCourseRepository.existsByStudentIdAndCourseId(1L, 2L)).isTrue();
        assertThat(studentCourseRepository.findByStudentId(1L)).hasSize(1);
        assertThat(studentCourseRepository.findByCourseId(2L)).hasSize(1);

        Teacher teacher = new Teacher();
        teacher.setName("Prof");
        teacher.setEmail("prof@example.com");
        teacher.setSubject("Java");
        assertThat(teacherRepository.save(teacher).getId()).isNotNull();

        User user = new User();
        user.setName("Admin");
        user.setEmail("admin@example.com");
        user.setPassword("secret");
        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
        assertThat(userRepository.findByEmail("admin@example.com")).isNotNull();
        assertThat(userRepository.findByRole(User.Role.ADMIN)).hasSize(1);

        QrSession session = new QrSession();
        session.setCourseId(2L);
        session.setQrCodeData("QR");
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());
        qrSessionRepository.save(session);
        qrSessionRepository.deleteByCourseId(2L);
        assertThat(qrSessionRepository.findAll()).isEmpty();
    }
}
