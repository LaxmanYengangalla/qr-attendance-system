package com.qrattendance.qr_attendance_system.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private static final String PREFIX = "sha256$";

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.startsWith(PREFIX)) {
            return rawPassword;
        }

        return PREFIX + sha256(rawPassword + getPepper());
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith(PREFIX)) {
            return hash(rawPassword).equals(storedPassword);
        }

        return storedPassword.equals(rawPassword);
    }

    private String getPepper() {
        return System.getProperty("APP_PASSWORD_PEPPER", "");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is not available", error);
        }
    }
}
