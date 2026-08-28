package com.healthcare.gui.panels;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.gui.ModernTheme;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel for Doctors to manage consultation queues, record diagnosis notes, and issue prescriptions.
 */
public class DoctorPanel extends JPanel {
    private final Doctor doctor;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;

    private DefaultTableModel queueTableModel;
    private JTable queueTable;

    public DoctorPanel(Doctor doctor, AppointmentService appointmentService, PrescriptionService prescriptionService) {
        this.doctor = doctor;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Top Card Header
        JPanel headerCard = ModernTheme.createCardPanel();
        headerCard.setLayout(new BorderLayout(12, 0));

        JLabel titleLbl = new JLabel("Doctor Consultation Queue & Patients - Dr. " + doctor.getFullName());
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        JLabel subLbl = new JLabel("Specialization: " + doctor.getSpecialization() + " | Department: " + doctor.getDepartment() + " | Room: " + doctor.getRoomNumber());
        subLbl.setFont(ModernTheme.FONT_SMALL);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLbl);
        textPanel.add(subLbl);

        JButton refreshBtn = ModernTheme.createPrimaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> refreshTable());

        headerCard.add(textPanel, BorderLayout.WEST);
        headerCard.add(refreshBtn, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = ModernTheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] columns = {"Appt ID", "Patient Name", "Patient ID", "Date", "Time", "Reason for Visit", "Status", "Notes"};
        queueTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        queueTable = new JTable(queueTableModel);
        ModernTheme.styleTable(queueTable);

        // Set column widths for clean alignment
        queueTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        queueTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        queueTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        queueTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        queueTable.getColumnModel().getColumn(7).setPreferredWidth(180);

        tableCard.add(new JScrollPane(queueTable), BorderLayout.CENTER);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JButton viewDetailsBtn = ModernTheme.createSecondaryButton("View Full Details");
        viewDetailsBtn.addActionListener(e -> handleViewFullDetails());

        JButton rescheduleBtn = ModernTheme.createSecondaryButton("Reschedule Slot");
        rescheduleBtn.addActionListener(e -> showDoctorRescheduleDialog());

        JButton startConsultationBtn = ModernTheme.createPrimaryButton("Start Consultation");
        startConsultationBtn.addActionListener(e -> setStatus(AppointmentStatus.IN_CONSULTATION));

        JButton completeConsultationBtn = ModernTheme.createPrimaryButton("Complete Consultation & Notes");
        completeConsultationBtn.setBackground(ModernTheme.STATUS_COMPLETED);
        completeConsultationBtn.addActionListener(e -> showConsultationDialog());

        JButton issuePrescriptionBtn = ModernTheme.createPrimaryButton("Issue Prescription");
        issuePrescriptionBtn.setBackground(ModernTheme.ACCENT);
        issuePrescriptionBtn.addActionListener(e -> showPrescriptionDialog());

        toolbar.add(viewDetailsBtn);
        toolbar.add(rescheduleBtn);
        toolbar.add(startConsultationBtn);
        toolbar.add(completeConsultationBtn);
        toolbar.add(issuePrescriptionBtn);

        tableCard.add(toolbar, BorderLayout.SOUTH);
        add(tableCard, BorderLayout.CENTER);

        // Double-click row listener to view full details easily
        queueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewFullDetails();
                }
            }
        });
    }

    private void handleViewFullDetails() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        Appointment appt = appointmentService.getAppointmentsForUser(doctor).stream()
            .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);

        if (appt != null) {
            showFullRecordDialog(appt);
        }
    }

    private void showFullRecordDialog(Appointment appt) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Patient Record Details - " + appt.getAppointmentId(), true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addFormRow(panel, gbc, "Appointment ID:", new JLabel(appt.getAppointmentId()), y++);
        addFormRow(panel, gbc, "Patient Name:", new JLabel(appt.getPatientName() + " (" + appt.getPatientId() + ")"), y++);
        addFormRow(panel, gbc, "Date & Time:", new JLabel(appt.getAppointmentDate() + " at " + appt.getAppointmentTime()), y++);
        addFormRow(panel, gbc, "Status:", new JLabel(appt.getStatus().getLabel()), y++);
        addFormRow(panel, gbc, "Consultation Fee:", new JLabel(String.format("RM %.2f", appt.getFee())), y++);

        JTextArea reasonArea = new JTextArea(appt.getReason());
        reasonArea.setEditable(false);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(ModernTheme.FONT_REGULAR);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Reason for Visit:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(new JScrollPane(reasonArea), gbc);

        JTextArea notesArea = new JTextArea(appt.getConsultationNotes() != null ? appt.getConsultationNotes() : "No notes recorded.");
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ModernTheme.FONT_REGULAR);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Diagnosis & Notes:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(new JScrollPane(notesArea), gbc);

        JButton closeBtn = ModernTheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(closeBtn, gbc);

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

    private void setStatus(AppointmentStatus status) {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the queue table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        try {
            appointmentService.updateStatus(apptId, status);
            JOptionPane.showMessageDialog(this, "Status updated to: " + status.getLabel(), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Status Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showConsultationDialog() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        String patName = (String) queueTableModel.getValueAt(row, 1);
        String existingNotes = (String) queueTableModel.getValueAt(row, 7);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Consultation - " + patName, true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titleLbl = new JLabel("Consultation Diagnosis & Notes:");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        panel.add(titleLbl, BorderLayout.NORTH);

        JTextArea notesArea = new JTextArea(existingNotes != null && !existingNotes.equals("-") ? existingNotes : "");
        notesArea.setFont(ModernTheme.FONT_REGULAR);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        JButton saveBtn = ModernTheme.createPrimaryButton("Save & Mark Completed");
        saveBtn.addActionListener(e -> {
            String notesText = notesArea.getText().trim();
            if (notesText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Consultation notes cannot be left blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                appointmentService.recordConsultation(apptId, notesText);
                JOptionPane.showMessageDialog(dialog, "Consultation record saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(saveBtn, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showPrescriptionDialog() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        String patName = (String) queueTableModel.getValueAt(row, 1);
        String patId = (String) queueTableModel.getValueAt(row, 2);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Issue Prescription - " + patName, true);
        dialog.setSize(460, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField medField = ModernTheme.createTextField();
        JTextField dosageField = ModernTheme.createTextField();
        JTextField instructionsField = ModernTheme.createTextField();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Medication Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panel.add(medField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Dosage (e.g. 500mg):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        panel.add(dosageField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(new JLabel("Instructions:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        panel.add(instructionsField, gbc);

        JButton issueBtn = ModernTheme.createPrimaryButton("Send to Pharmacy");
        issueBtn.addActionListener(e -> {
            String med = medField.getText().trim();
            String dosage = dosageField.getText().trim();
            String inst = instructionsField.getText().trim();

            if (med.isEmpty() || dosage.isEmpty() || inst.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Medication name, dosage, and instructions must all be filled in.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                prescriptionService.createPrescription(
                    apptId, patId, patName, doctor.getUserId(), doctor.getFullName(), 
                    med, dosage, inst
                );
                JOptionPane.showMessageDialog(dialog, "Prescription issued to pharmacy successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(issueBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    public void refreshTable() {
        queueTableModel.setRowCount(0);
        List<Appointment> appts = appointmentService.getAppointmentsForUser(doctor);
        for (Appointment a : appts) {
            queueTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getPatientId(),
                a.getAppointmentDate().toString(),
                a.getAppointmentTime().toString(),
                a.getReason(),
                a.getStatus().getLabel(),
                a.getConsultationNotes() != null ? a.getConsultationNotes() : "-"
            });
        }
    }

    private void showDoctorRescheduleDialog() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reschedule Appointment - " + apptId, true);
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] timeSlots = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00"};
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        timeCombo.setSelectedItem("11:00");
        JTextField dateField = ModernTheme.createTextField();
        dateField.setText(java.time.LocalDate.now().plusDays(1).toString());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("New Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("New Time Slot:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        panel.add(timeCombo, gbc);

        JButton saveBtn = ModernTheme.createPrimaryButton("Confirm Reschedule");
        saveBtn.addActionListener(e -> {
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(dateField.getText().trim());
                java.time.LocalTime t = java.time.LocalTime.parse((String) timeCombo.getSelectedItem());
                appointmentService.rescheduleAppointment(apptId, d, t);
                JOptionPane.showMessageDialog(dialog, "Appointment rescheduled successfully by Dr. " + doctor.getFullName() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Reschedule Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(saveBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
