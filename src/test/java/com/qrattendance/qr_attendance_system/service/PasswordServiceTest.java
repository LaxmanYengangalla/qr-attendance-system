package com.qrattendance.qr_attendance_system.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @AfterEach
    void clearPepper() {
        System.clearProperty("APP_PASSWORD_PEPPER");
    }

    @Test
    void hashStoresPasswordWithPrefixAndMatchesRawPassword() {
        System.setProperty("APP_PASSWORD_PEPPER", "pepper");

        String hashed = passwordService.hash("secret");

        assertThat(hashed).startsWith("sha256$");
        assertThat(hashed).isNotEqualTo("secret");
        assertThat(passwordService.matches("secret", hashed)).isTrue();
        assertThat(passwordService.matches("wrong", hashed)).isFalse();
    }

    @Test
    void matchesLegacyPlainTextPassword() {
        assertThat(passwordService.matches("old-password", "old-password")).isTrue();
        assertThat(passwordService.matches("new-password", "old-password")).isFalse();
    }
}
