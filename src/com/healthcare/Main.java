package com.healthcare;

import com.healthcare.gui.LoginFrame;
import com.healthcare.gui.MainDashboardFrame;
import com.healthcare.service.*;

import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Smart Healthcare System.
 */
public class Main {
    private static DataStorageService dataStorageService;
    private static AuthService authService;
    private static AppointmentService appointmentService;
    private static PrescriptionService prescriptionService;
    private static NotificationService notificationService;
    private static ReportService reportService;

    public static void main(String[] args) {
        // Set cross-platform look and feel for consistent UI rendering
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default Swing Look & Feel
        }

        // Initialize Service Layer
        dataStorageService = new DataStorageService();
        notificationService = new NotificationService(dataStorageService);
        authService = new AuthService(dataStorageService);
        appointmentService = new AppointmentService(dataStorageService, notificationService);
        prescriptionService = new PrescriptionService(dataStorageService, notificationService);
        reportService = new ReportService(dataStorageService);

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(Main::showLoginScreen);
    }

    private static void showLoginScreen() {
        LoginFrame loginFrame = new LoginFrame(authService, Main::showDashboard);
        loginFrame.setVisible(true);
    }

    private static void showDashboard() {
        MainDashboardFrame dashboardFrame = new MainDashboardFrame(
            authService, appointmentService, prescriptionService, notificationService, reportService, Main::showLoginScreen
        );
        dashboardFrame.setVisible(true);
    }
}
