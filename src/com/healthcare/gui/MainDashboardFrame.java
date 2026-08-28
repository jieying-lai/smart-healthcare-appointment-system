package com.healthcare.gui;

import com.healthcare.gui.panels.*;
import com.healthcare.model.*;
import com.healthcare.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Main Application Dashboard Frame adapting navigation tabs and views based on logged-in user role.
 */
public class MainDashboardFrame extends JFrame {
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final User currentUser;
    private final Runnable onLogout;

    private JTabbedPane mainTabbedPane;

    public MainDashboardFrame(AuthService authService, AppointmentService appointmentService, 
                              PrescriptionService prescriptionService, NotificationService notificationService, 
                              ReportService reportService, Runnable onLogout) {
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.currentUser = authService.getCurrentUser();
        this.onLogout = onLogout;

        setTitle("Smart Healthcare Management System - " + currentUser.getFullName() + " (" + currentUser.getRole().getDisplayName() + ")");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);

        // Header Navigation Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernTheme.PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLbl = new JLabel("Smart Healthcare Management System");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel userLbl = new JLabel("Logged in as: " + currentUser.getFullName() + " | Role: " + currentUser.getRole().getDisplayName());
        userLbl.setFont(ModernTheme.FONT_SMALL);
        userLbl.setForeground(new Color(224, 242, 254));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(titleLbl);
        titleBox.add(userLbl);

        header.add(titleBox, BorderLayout.WEST);

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionBox.setOpaque(false);

        long unreadNotifs = notificationService.getUnreadCount(currentUser.getUserId());
        JButton notifBtn = ModernTheme.createSecondaryButton("Notifications (" + unreadNotifs + ")");
        notifBtn.setBackground(Color.WHITE);
        notifBtn.setForeground(ModernTheme.PRIMARY_DARK);

        JButton profileBtn = ModernTheme.createSecondaryButton("My Profile");
        profileBtn.setBackground(Color.WHITE);
        profileBtn.setForeground(ModernTheme.PRIMARY_DARK);

        JButton logoutBtn = ModernTheme.createDangerButton("Logout");
        logoutBtn.addActionListener(e -> {
            authService.logout();
            dispose();
            onLogout.run();
        });

        notifBtn.addActionListener(e -> showNotificationsDialog(notifBtn));
        profileBtn.addActionListener(e -> showProfileDialog());

        actionBox.add(notifBtn);
        actionBox.add(profileBtn);
        actionBox.add(logoutBtn);

        header.add(actionBox, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Content: Role-Adaptive Tabs
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(ModernTheme.FONT_BOLD);
        mainTabbedPane.setBackground(ModernTheme.BACKGROUND);
        mainTabbedPane.setForeground(ModernTheme.PRIMARY_DARK);

        Role role = currentUser.getRole();

        if (role == Role.PATIENT) {
            mainTabbedPane.addTab("My Appointments & Bookings", new PatientPanel(authService, appointmentService, prescriptionService, (Patient) currentUser));
        } else if (role == Role.DOCTOR) {
            mainTabbedPane.addTab("Consultation Queue & Patients", new DoctorPanel((Doctor) currentUser, appointmentService, prescriptionService));
            mainTabbedPane.addTab("Executive Reports", new ReportPanel(reportService));
        } else if (role == Role.NURSE) {
            mainTabbedPane.addTab("Nurse Patient Check-In Queue", new NursePanel(appointmentService, (Nurse) currentUser));
        } else if (role == Role.PHARMACIST) {
            mainTabbedPane.addTab("Pharmacy Medication Dispensing", new PharmacistPanel(prescriptionService, (Pharmacist) currentUser));
        } else if (role == Role.ADMIN) {
            mainTabbedPane.addTab("User & Account Management", new AdminUserManagementPanel(authService));
            mainTabbedPane.addTab("Master Appointments View (Read-Only)", createAdminAppointmentsMasterView());
            mainTabbedPane.addTab("Master Prescriptions View (Read-Only)", createAdminPrescriptionsMasterView());
            mainTabbedPane.addTab("System Analytics & Reports", new ReportPanel(reportService));
        }

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createAdminAppointmentsMasterView() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel headerCard = ModernTheme.createCardPanel();
        headerCard.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("System Master Appointments Registry (Read-Only Overview)");
        lbl.setFont(ModernTheme.FONT_TITLE);
        JButton refreshBtn = ModernTheme.createPrimaryButton("Refresh Overview");
        headerCard.add(lbl, BorderLayout.WEST);
        headerCard.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerCard, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Doctor Name", "Date", "Time", "Reason for Visit", "Fee (RM)", "Status", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        ModernTheme.styleTable(table);

        Runnable loadData = () -> {
            model.setRowCount(0);
            for (Appointment a : appointmentService.getAllAppointments()) {
                model.addRow(new Object[]{
                    a.getAppointmentId(), a.getPatientName(), a.getDoctorName(),
                    a.getAppointmentDate().toString(), a.getAppointmentTime().toString(),
                    a.getReason(), String.format("RM %.2f", a.getFee()),
                    a.getStatus().getLabel(), a.getConsultationNotes() != null ? a.getConsultationNotes() : "-"
                });
            }
        };

        refreshBtn.addActionListener(e -> loadData.run());
        loadData.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminPrescriptionsMasterView() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel headerCard = ModernTheme.createCardPanel();
        headerCard.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("System Master Pharmacy Prescriptions Registry (Read-Only Overview)");
        lbl.setFont(ModernTheme.FONT_TITLE);
        JButton refreshBtn = ModernTheme.createPrimaryButton("Refresh Prescriptions");
        headerCard.add(lbl, BorderLayout.WEST);
        headerCard.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerCard, BorderLayout.NORTH);

        String[] cols = {"Prescription ID", "Appt ID", "Patient Name", "Doctor Name", "Medication Name", "Dosage", "Instructions", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        ModernTheme.styleTable(table);

        Runnable loadData = () -> {
            model.setRowCount(0);
            for (Prescription rx : prescriptionService.getAllPrescriptions()) {
                model.addRow(new Object[]{
                    rx.getPrescriptionId(), rx.getAppointmentId(), rx.getPatientName(),
                    rx.getDoctorName(), rx.getMedicationName(), rx.getDosage(),
                    rx.getInstructions(), rx.getStatus().getLabel()
                });
            }
        };

        refreshBtn.addActionListener(e -> loadData.run());
        loadData.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showNotificationsDialog(JButton notifBtn) {
        List<Notification> notifs = notificationService.getNotificationsForUser(currentUser.getUserId());
        notificationService.markAllAsRead(currentUser.getUserId());
        notifBtn.setText("Notifications (0)");

        JDialog dialog = new JDialog(this, "System Notifications & Alerts", true);
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Your Recent Notifications (" + notifs.size() + ")");
        title.setFont(ModernTheme.FONT_TITLE);
        panel.add(title, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (notifs.isEmpty()) {
            listModel.addElement("No notifications found.");
        } else {
            for (Notification n : notifs) {
                listModel.addElement("[" + n.getFormattedTimestamp() + "] [" + n.getType() + "] " + n.getTitle() + ": " + n.getMessage());
            }
        }

        JList<String> list = new JList<>(listModel);
        list.setFont(ModernTheme.FONT_REGULAR);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton closeBtn = ModernTheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "User Profile & Contact Information", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = ModernTheme.createTextField();
        emailField.setText(currentUser.getEmail());

        JTextField phoneField = ModernTheme.createTextField();
        phoneField.setText(currentUser.getPhone());

        JPasswordField newPassField = ModernTheme.createPasswordField();

        int y = 0;
        addFormRow(panel, gbc, "Full Name:", new JLabel(currentUser.getFullName()), y++);
        addFormRow(panel, gbc, "User Role:", new JLabel(currentUser.getRole().getDisplayName()), y++);
        addFormRow(panel, gbc, "Email Address:", emailField, y++);
        addFormRow(panel, gbc, "Phone Number:", phoneField, y++);
        addFormRow(panel, gbc, "New Password (Optional):", newPassField, y++);

        JButton saveBtn = ModernTheme.createPrimaryButton("Save Profile Changes");
        saveBtn.addActionListener(e -> {
            try {
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPhone(phoneField.getText().trim());

                String pass = new String(newPassField.getPassword());
                if (!pass.trim().isEmpty()) {
                    currentUser.setPassword(pass.trim());
                }

                authService.updateUserProfile(currentUser);
                JOptionPane.showMessageDialog(dialog, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }
}
