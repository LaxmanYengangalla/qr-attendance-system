package com.qrattendance.qr_attendance_system.service;

import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QrService {

    @Autowired
    private QRSessionRepository qrSessionRepository;

    public Long createSession(Long courseId) {

        QrSession session = new QrSession();

        session.setCourseId(courseId);

        session.setQrCodeData("QR_SESSION_" + courseId);

        session.setStartTime(LocalDateTime.now());

        session.setEndTime(LocalDateTime.now().plusMinutes(2));

        session.setCreatedBy(1L);

        QrSession savedSession =
                qrSessionRepository.save(session);

        return savedSession.getId();
    }
}