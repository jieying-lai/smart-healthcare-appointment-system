package com.healthcare.gui;

import com.healthcare.gui.panels.*;
import com.healthcare.model.*;
import com.healthcare.service.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        setSize(1120, 740);
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
            // DOCTOR ROLE: Focus strictly on Patient Consultation Queue & Prescriptions (NO Admin Reports)
            mainTabbedPane.addTab("Consultation Queue & Patients", new DoctorPanel((Doctor) currentUser, appointmentService, prescriptionService));
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
        JLabel lbl = new JLabel("System Master Appointments Registry (Double-click any row to view full details)");
        lbl.setFont(ModernTheme.FONT_TITLE);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        JButton viewBtn = ModernTheme.createSecondaryButton("View Full Details");
        JButton refreshBtn = ModernTheme.createPrimaryButton("Refresh Overview");
        btnBox.add(viewBtn);
        btnBox.add(refreshBtn);

        headerCard.add(lbl, BorderLayout.WEST);
        headerCard.add(btnBox, BorderLayout.EAST);
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

        Runnable showDetails = () -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Please select an appointment from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String apptId = (String) model.getValueAt(row, 0);
            Appointment appt = appointmentService.getAllAppointments().stream()
                .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);
            if (appt != null) {
                showFullAppointmentDialog(appt);
            }
        };

        viewBtn.addActionListener(e -> showDetails.run());
        refreshBtn.addActionListener(e -> loadData.run());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetails.run();
            }
        });

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
        JLabel lbl = new JLabel("System Master Pharmacy Prescriptions Registry (Double-click any row for full details)");
        lbl.setFont(ModernTheme.FONT_TITLE);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        JButton viewBtn = ModernTheme.createSecondaryButton("View Full Details");
        JButton refreshBtn = ModernTheme.createPrimaryButton("Refresh Prescriptions");
        btnBox.add(viewBtn);
        btnBox.add(refreshBtn);

        headerCard.add(lbl, BorderLayout.WEST);
        headerCard.add(btnBox, BorderLayout.EAST);
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

        Runnable showDetails = () -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a prescription from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String rxId = (String) model.getValueAt(row, 0);
            Prescription rx = prescriptionService.getAllPrescriptions().stream()
                .filter(r -> r.getPrescriptionId().equals(rxId)).findFirst().orElse(null);
            if (rx != null) {
                showFullPrescriptionDialog(rx);
            }
        };

        viewBtn.addActionListener(e -> showDetails.run());
        refreshBtn.addActionListener(e -> loadData.run());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetails.run();
            }
        });

        loadData.run();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showNotificationsDialog(JButton notifBtn) {
        List<Notification> notifs = notificationService.getNotificationsForUser(currentUser.getUserId());
        notificationService.markAllAsRead(currentUser.getUserId());
        notifBtn.setText("Notifications (0)");

        JDialog dialog = new JDialog(this, "System Notifications & Alerts", true);
        dialog.setSize(580, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(ModernTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Notification Center (" + notifs.size() + " messages)");
        title.setFont(ModernTheme.FONT_TITLE);
        panel.add(title, BorderLayout.NORTH);

        JPanel cardsList = new JPanel();
        cardsList.setLayout(new BoxLayout(cardsList, BoxLayout.Y_AXIS));
        cardsList.setOpaque(false);

        if (notifs.isEmpty()) {
            JPanel emptyCard = ModernTheme.createCardPanel();
            emptyCard.add(new JLabel("No notifications available."));
            cardsList.add(emptyCard);
        } else {
            for (Notification n : notifs) {
                JPanel card = new JPanel(new BorderLayout(8, 4));
                card.setBackground(Color.WHITE);
                card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));

                JLabel typeTag = new JLabel(" [" + n.getType() + "] " + n.getTitle() + " ");
                typeTag.setFont(ModernTheme.FONT_BOLD);
                typeTag.setOpaque(true);
                typeTag.setBackground(ModernTheme.PRIMARY_LIGHT);
                typeTag.setForeground(ModernTheme.PRIMARY_DARK);

                JLabel timeLbl = new JLabel(n.getFormattedTimestamp());
                timeLbl.setFont(ModernTheme.FONT_SMALL);

                JPanel cardHeader = new JPanel(new BorderLayout());
                cardHeader.setOpaque(false);
                cardHeader.add(typeTag, BorderLayout.WEST);
                cardHeader.add(timeLbl, BorderLayout.EAST);

                JTextArea msgArea = new JTextArea(n.getMessage());
                msgArea.setFont(ModernTheme.FONT_REGULAR);
                msgArea.setLineWrap(true);
                msgArea.setWrapStyleWord(true);
                msgArea.setEditable(false);
                msgArea.setOpaque(false);
                msgArea.setBorder(new EmptyBorder(4, 0, 0, 0));

                card.add(cardHeader, BorderLayout.NORTH);
                card.add(msgArea, BorderLayout.CENTER);

                cardsList.add(card);
                cardsList.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JScrollPane scroll = new JScrollPane(cardsList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ModernTheme.BACKGROUND);
        panel.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = ModernTheme.createPrimaryButton("Close Notification Center");
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showFullAppointmentDialog(Appointment appt) {
        JDialog dialog = new JDialog(this, "Appointment Record Details - " + appt.getAppointmentId(), true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addFormRow(panel, gbc, "Appointment ID:", new JLabel(appt.getAppointmentId()), y++);
        addFormRow(panel, gbc, "Patient Name:", new JLabel(appt.getPatientName() + " (" + appt.getPatientId() + ")"), y++);
        addFormRow(panel, gbc, "Doctor Name:", new JLabel(appt.getDoctorName() + " (" + appt.getDoctorId() + ")"), y++);
        addFormRow(panel, gbc, "Date & Time:", new JLabel(appt.getAppointmentDate() + " at " + appt.getAppointmentTime()), y++);
        addFormRow(panel, gbc, "Status:", new JLabel(appt.getStatus().getLabel()), y++);
        addFormRow(panel, gbc, "Consultation Fee:", new JLabel(String.format("RM %.2f", appt.getFee())), y++);
        addFormRow(panel, gbc, "Reason for Visit:", new JLabel(appt.getReason()), y++);

        JTextArea notesArea = new JTextArea(appt.getConsultationNotes() != null ? appt.getConsultationNotes() : "No notes recorded.");
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ModernTheme.FONT_REGULAR);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel notesLbl = new JLabel("Consultation Notes:");
        notesLbl.setFont(ModernTheme.FONT_BOLD);
        panel.add(notesLbl, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(new JScrollPane(notesArea), gbc);

        JButton closeBtn = ModernTheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(closeBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showFullPrescriptionDialog(Prescription rx) {
        JDialog dialog = new JDialog(this, "Prescription Details - " + rx.getPrescriptionId(), true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addFormRow(panel, gbc, "Prescription ID:", new JLabel(rx.getPrescriptionId()), y++);
        addFormRow(panel, gbc, "Patient Name:", new JLabel(rx.getPatientName()), y++);
        addFormRow(panel, gbc, "Prescribing Doctor:", new JLabel(rx.getDoctorName()), y++);
        addFormRow(panel, gbc, "Dispensing Pharmacy:", new JLabel(rx.getDispensedByPharmacistName() + " (" + rx.getPharmacySection() + ")"), y++);
        addFormRow(panel, gbc, "Medication Name:", new JLabel(rx.getMedicationName()), y++);
        addFormRow(panel, gbc, "Dosage:", new JLabel(rx.getDosage()), y++);
        addFormRow(panel, gbc, "Status:", new JLabel(rx.getStatus().getLabel()), y++);

        JTextArea instArea = new JTextArea(rx.getInstructions() != null ? rx.getInstructions() : "No special instructions.");
        instArea.setEditable(false);
        instArea.setLineWrap(true);
        instArea.setWrapStyleWord(true);
        instArea.setFont(ModernTheme.FONT_REGULAR);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel instLbl = new JLabel("Instructions:");
        instLbl.setFont(ModernTheme.FONT_BOLD);
        panel.add(instLbl, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(new JScrollPane(instArea), gbc);

        JButton closeBtn = ModernTheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(closeBtn, gbc);

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
