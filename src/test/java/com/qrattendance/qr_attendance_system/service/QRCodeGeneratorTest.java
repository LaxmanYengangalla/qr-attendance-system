package com.qrattendance.qr_attendance_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class QRCodeGeneratorTest {

    @Test
    void generateQRCodeImageReturnsExpectedDimensions() throws Exception {
        BufferedImage image = QRCodeGenerator.generateQRCode("attendance-session-1", 240, 180);

        assertThat(image.getWidth()).isEqualTo(240);
        assertThat(image.getHeight()).isEqualTo(180);
    }

    @Test
    void generateQRCodeImageRejectsBlankInput() {
        assertThatThrownBy(() -> QRCodeGenerator.generateQRCode("", 200, 200))
                .isInstanceOf(Exception.class);
    }
}
