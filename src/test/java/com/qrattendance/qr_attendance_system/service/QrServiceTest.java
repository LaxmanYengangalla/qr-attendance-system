package com.qrattendance.qr_attendance_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qrattendance.qr_attendance_system.model.QrSession;
import com.qrattendance.qr_attendance_system.repository.QRSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QrServiceTest {

    @Mock
    private QRSessionRepository qrSessionRepository;

    @InjectMocks
    private QrService qrService;

    @Test
    void createSessionSavesQrSessionAndReturnsId() {
        when(qrSessionRepository.save(any(QrSession.class))).thenAnswer(invocation -> {
            QrSession session = invocation.getArgument(0);
            ReflectionTestUtils.setField(session, "id", 77L);
            return session;
        });

        Long id = qrService.createSession(5L);

        assertThat(id).isEqualTo(77L);
        verify(qrSessionRepository).save(any(QrSession.class));
    }
}
