package com.qrattendance.qr_attendance_system;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupMessage {

    @Value("${server.port}")
    private String port;

    @EventListener(ApplicationReadyEvent.class)
    public void printUrl() {

        System.out.println("\n=================================");
        System.out.println("Application Started Successfully!");
        System.out.println("URL: http://localhost:" + port);
        System.out.println("=================================\n");
    }
}