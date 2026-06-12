package com.qrattendance.qr_attendance_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QrAttendanceSystemApplication {

    public static void main(String[] args) {
        EnvLoader.load();
        SpringApplication.run(QrAttendanceSystemApplication.class, args);
    }
}
