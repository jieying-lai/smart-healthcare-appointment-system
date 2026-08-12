package com.healthcare.gui.panels;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.exception.SlotConflictException;
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
 * Dashboard panel for Patients to book, reschedule, cancel appointments and view prescriptions.
 */
public class PatientPanel extends JPanel {
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final Patient patient;

    private JTable appointmentTable;
    private DefaultTableModel appointmentTableModel;
    private JTable prescriptionTable;
    private DefaultTableModel prescriptionTableModel;

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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);

        // Tab 1: Appointments
        JPanel apptPanel = new JPanel(new BorderLayout(0, 12));
        apptPanel.setOpaque(false);

        // Top Toolbar
        JPanel toolbar = ModernTheme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JButton bookBtn = ModernTheme.createPrimaryButton("+ Book New Appointment");
        bookBtn.addActionListener(e -> showBookDialog());

        JButton rescheduleBtn = ModernTheme.createSecondaryButton("Reschedule Selected");
        rescheduleBtn.addActionListener(e -> handleReschedule());

        JButton cancelBtn = ModernTheme.createDangerButton("Cancel Appointment");
        cancelBtn.addActionListener(e -> handleCancel());

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTables());

        toolbar.add(bookBtn);
        toolbar.add(rescheduleBtn);
        toolbar.add(cancelBtn);
        toolbar.add(refreshBtn);

        apptPanel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Appt ID", "Doctor", "Date", "Time", "Status", "Reason", "Consultation Notes"};
        appointmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appointmentTable = new JTable(appointmentTableModel);
        ModernTheme.styleTable(appointmentTable);

        apptPanel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        tabbedPane.addTab("My Appointments", apptPanel);

        // Tab 2: Prescriptions
        JPanel rxPanel = new JPanel(new BorderLayout(0, 12));
        rxPanel.setOpaque(false);

        String[] rxCols = {"Prescription ID", "Doctor", "Medication Name", "Dosage", "Instructions", "Status", "Issued Date"};
        prescriptionTableModel = new DefaultTableModel(rxCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        prescriptionTable = new JTable(prescriptionTableModel);
        ModernTheme.styleTable(prescriptionTable);

        rxPanel.add(new JScrollPane(prescriptionTable), BorderLayout.CENTER);
        tabbedPane.addTab("My Prescriptions & Medications", rxPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshTables() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> appts = appointmentService.getAppointmentsForUser(patient);
        for (Appointment a : appts) {
            appointmentTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getDoctorName(),
                a.getFormattedDate(),
                a.getFormattedTime(),
                a.getStatus(),
                a.getReasonForVisit(),
                a.getConsultationNotes()
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

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Appointment", true);
        dialog.setSize(420, 360);
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

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(docCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Reason for Visit:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(reasonField, gbc);

        JButton confirmBtn = ModernTheme.createPrimaryButton("Confirm Booking");
        confirmBtn.addActionListener(e -> {
            try {
                Doctor selectedDoc = (Doctor) docCombo.getSelectedItem();
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                LocalTime t = LocalTime.parse(timeField.getText().trim());
                String reason = reasonField.getText().trim();

                appointmentService.bookAppointment(patient.getUserId(), selectedDoc.getUserId(), d, t, reason);
                JOptionPane.showMessageDialog(dialog, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTables();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Booking Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(confirmBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleReschedule() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) appointmentTableModel.getValueAt(selectedRow, 0);

        String newDateStr = JOptionPane.showInputDialog(this, "Enter New Date (YYYY-MM-DD):", LocalDate.now().plusDays(2).toString());
        if (newDateStr == null || newDateStr.trim().isEmpty()) return;

        String newTimeStr = JOptionPane.showInputDialog(this, "Enter New Time (HH:MM):", "14:00");
        if (newTimeStr == null || newTimeStr.trim().isEmpty()) return;

        try {
            LocalDate newDate = LocalDate.parse(newDateStr.trim());
            LocalTime newTime = LocalTime.parse(newTimeStr.trim());

            appointmentService.rescheduleAppointment(apptId, newDate, newTime);
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Reschedule Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) appointmentTableModel.getValueAt(selectedRow, 0);
        String reason = JOptionPane.showInputDialog(this, "Reason for cancellation:");
        if (reason == null) return;

        try {
            appointmentService.cancelAppointment(apptId, reason);
            JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cancellation Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
