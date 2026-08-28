package com.healthcare.gui.panels;

import com.healthcare.gui.ModernTheme;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.AuthService;
import com.healthcare.service.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Dashboard panel for Patients to view personal appointments, book new slots, and view issued prescriptions.
 */
public class PatientPanel extends JPanel {
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final Patient patient;

    private DefaultTableModel appointmentTableModel;
    private JTable appointmentTable;
    private DefaultTableModel prescriptionTableModel;
    private JTable prescriptionTable;

    public PatientPanel(AuthService authService, AppointmentService appointmentService, 
                        PrescriptionService prescriptionService, Patient patient) {
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
        this.patient = patient;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshTables();
    }

    private void initUI() {
        JTabbedPane subPane = new JTabbedPane();
        subPane.setFont(ModernTheme.FONT_BOLD);

        // Tab 1: Appointments
        JPanel apptTab = new JPanel(new BorderLayout(0, 12));
        apptTab.setBackground(ModernTheme.BACKGROUND);
        apptTab.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel apptToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        apptToolbar.setOpaque(false);

        JButton bookBtn = ModernTheme.createPrimaryButton("+ Book New Appointment");
        bookBtn.addActionListener(e -> showBookDialog());

        JButton rescheduleBtn = ModernTheme.createSecondaryButton("Reschedule Selected");
        rescheduleBtn.addActionListener(e -> showRescheduleDialog());

        JButton cancelBtn = ModernTheme.createDangerButton("Cancel Appointment");
        cancelBtn.addActionListener(e -> handleCancelAppointment());

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTables());

        apptToolbar.add(bookBtn);
        apptToolbar.add(rescheduleBtn);
        apptToolbar.add(cancelBtn);
        apptToolbar.add(refreshBtn);

        apptTab.add(apptToolbar, BorderLayout.NORTH);

        String[] apptCols = {"Appt ID", "Doctor Name", "Date", "Time", "Reason for Visit", "Fee (RM)", "Status", "Notes"};
        appointmentTableModel = new DefaultTableModel(apptCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appointmentTable = new JTable(appointmentTableModel);
        ModernTheme.styleTable(appointmentTable);

        apptTab.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        subPane.addTab("My Appointments", apptTab);

        // Tab 2: Prescriptions
        JPanel rxTab = new JPanel(new BorderLayout(0, 12));
        rxTab.setBackground(ModernTheme.BACKGROUND);
        rxTab.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] rxCols = {"Prescription ID", "Doctor Name", "Medication Name", "Dosage", "Instructions", "Status", "Issued Date"};
        prescriptionTableModel = new DefaultTableModel(rxCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        prescriptionTable = new JTable(prescriptionTableModel);
        ModernTheme.styleTable(prescriptionTable);

        rxTab.add(new JScrollPane(prescriptionTable), BorderLayout.CENTER);
        subPane.addTab("My Prescriptions & Medications", rxTab);

        add(subPane, BorderLayout.CENTER);
    }

    public void refreshTables() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> appts = appointmentService.getAppointmentsForUser(patient);
        for (Appointment a : appts) {
            appointmentTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getDoctorName(),
                a.getAppointmentDate().toString(),
                a.getAppointmentTime().toString(),
                a.getReason(),
                String.format("RM %.2f", a.getFee()),
                a.getStatus().getLabel(),
                a.getConsultationNotes() != null ? a.getConsultationNotes() : "-"
            });
        }

        prescriptionTableModel.setRowCount(0);
        List<Prescription> rxs = prescriptionService.getPrescriptionsForPatient(patient.getUserId());
        for (Prescription rx : rxs) {
            prescriptionTableModel.addRow(new Object[]{
                rx.getPrescriptionId(),
                rx.getDoctorName(),
                rx.getMedicationName(),
                rx.getDosage(),
                rx.getInstructions(),
                rx.getStatus().getLabel(),
                rx.getFormattedIssuedAt()
            });
        }
    }

    private void showBookDialog() {
        List<Doctor> doctors = authService.getAllDoctors();
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors currently available.", "Booking Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book New Appointment", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Doctor> docCombo = new JComboBox<>(doctors.toArray(new Doctor[0]));
        JTextField dateField = ModernTheme.createTextField();
        dateField.setText(LocalDate.now().plusDays(1).toString());
        JTextField timeField = ModernTheme.createTextField();
        timeField.setText("10:00");
        JTextField reasonField = ModernTheme.createTextField();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panel.add(docCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        panel.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panel.add(new JLabel("Reason for Visit:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        panel.add(reasonField, gbc);

        JButton confirmBtn = ModernTheme.createPrimaryButton("Confirm Booking");
        confirmBtn.addActionListener(e -> {
            try {
                Doctor selectedDoc = (Doctor) docCombo.getSelectedItem();
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                LocalTime t = LocalTime.parse(timeField.getText().trim());
                String reason = reasonField.getText().trim();

                if (reason.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Reason for visit cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                appointmentService.bookAppointment(patient.getUserId(), selectedDoc.getUserId(), d, t, reason);
                JOptionPane.showMessageDialog(dialog, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(confirmBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showRescheduleDialog() {
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) appointmentTableModel.getValueAt(row, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reschedule Appointment - " + apptId, true);
        dialog.setSize(380, 240);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField dateField = ModernTheme.createTextField();
        dateField.setText(LocalDate.now().plusDays(2).toString());
        JTextField timeField = ModernTheme.createTextField();
        timeField.setText("11:00");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("New Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("New Time (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        panel.add(timeField, gbc);

        JButton saveBtn = ModernTheme.createPrimaryButton("Confirm Reschedule");
        saveBtn.addActionListener(e -> {
            try {
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                LocalTime t = LocalTime.parse(timeField.getText().trim());
                appointmentService.rescheduleAppointment(apptId, d, t);
                JOptionPane.showMessageDialog(dialog, "Appointment rescheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Reschedule Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleCancelAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) appointmentTableModel.getValueAt(row, 0);
        String reason = JOptionPane.showInputDialog(this, "Please state the reason for cancelling appointment " + apptId + ":", "Cancel Appointment", JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                appointmentService.cancelAppointment(apptId, reason.trim());
                JOptionPane.showMessageDialog(this, "Appointment cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
