package com.healthcare.gui;

import com.healthcare.gui.panels.*;
import com.healthcare.model.*;
import com.healthcare.service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern Multi-Role Main Dashboard Frame.
 * Adjusts accessible panels and permissions based on logged-in User Role.
 */
public class MainDashboardFrame extends JFrame {
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final NotificationService notificationService;
    private final ReportService reportService;

    private User currentUser;
    private Runnable onLogout;

    private JLabel notificationBadge;
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

        setTitle("Smart Healthcare System - Logged in as " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernTheme.PRIMARY); // Sky Blue Header Bar
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setOpaque(false);

        JLabel nameLbl = new JLabel("Welcome, " + currentUser.getFullName());
        nameLbl.setFont(ModernTheme.FONT_HEADER);
        nameLbl.setForeground(Color.WHITE);

        JLabel roleDescLbl = new JLabel(currentUser.getRoleDescription());
        roleDescLbl.setFont(ModernTheme.FONT_REGULAR);
        roleDescLbl.setForeground(ModernTheme.PRIMARY_LIGHT);

        userBox.add(nameLbl);
        userBox.add(Box.createRigidArea(new Dimension(0, 4)));
        userBox.add(roleDescLbl);

        header.add(userBox, BorderLayout.WEST);

        // Right Action Toolbar (Notifications, Profile, Logout)
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
            mainTabbedPane.addTab("Patient Dashboard", new PatientPanel(authService, appointmentService, prescriptionService, (Patient) currentUser));
        } else if (role == Role.DOCTOR) {
            mainTabbedPane.addTab("Consultation Queue & Patients", new DoctorPanel(appointmentService, prescriptionService, (Doctor) currentUser));
            mainTabbedPane.addTab("Executive Reports", new ReportPanel(reportService));
        } else if (role == Role.NURSE) {
            mainTabbedPane.addTab("Nurse Patient Check-In Queue", new NursePanel(appointmentService, (Nurse) currentUser));
        } else if (role == Role.PHARMACIST) {
            mainTabbedPane.addTab("Pharmacy Medication Dispensing", new PharmacistPanel(prescriptionService, (Pharmacist) currentUser));
        } else if (role == Role.ADMIN) {
            mainTabbedPane.addTab("User & Account Management", new AdminUserManagementPanel(authService));
            mainTabbedPane.addTab("System Appointments Master View", new PatientPanel(authService, appointmentService, prescriptionService, new Patient("ADM-PAT", "admin_pat", "pass", "System Overview Patient", "a@a.com", "123", "2000-01-01", "O+", "None", "123")));
            mainTabbedPane.addTab("Pharmacy Dispensing Master View", new PharmacistPanel(prescriptionService, new Pharmacist("ADM-PH", "admin_ph", "pass", "System Pharmacist", "a@a.com", "123", "LIC-999", "Main")));
            mainTabbedPane.addTab("System Analytics & Reports", new ReportPanel(reportService));
        }

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void showNotificationsDialog(JButton notifBtn) {
        List<Notification> notifs = notificationService.getNotificationsForUser(currentUser.getUserId());
        notificationService.markAllAsRead(currentUser.getUserId());
        notifBtn.setText("🔔 Notifications (0)");

        JDialog dialog = new JDialog(this, "Notifications & System Alerts", true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titleLbl = new JLabel("System Alerts & Reminder Notifications");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        panel.add(titleLbl, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (notifs.isEmpty()) {
            listModel.addElement("No notifications found.");
        } else {
            for (Notification n : notifs) {
                listModel.addElement("<html><b>[" + n.getFormattedTimestamp() + "] " + n.getTitle() + "</b><br>" + n.getMessage() + "<br></html>");
            }
        }

        JList<String> list = new JList<>(listModel);
        list.setFont(ModernTheme.FONT_REGULAR);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

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
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());

            String pass = new String(newPassField.getPassword());
            if (!pass.trim().isEmpty()) {
                currentUser.setPassword(pass.trim());
            }

            authService.updateUserProfile(currentUser);
            JOptionPane.showMessageDialog(dialog, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row;
        panel.add(comp, gbc);
    }
}
