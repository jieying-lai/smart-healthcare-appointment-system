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
        JLabel subLbl = new JLabel("Specialization: " + doctor.getSpecialization() + " | Room: " + doctor.getRoomNumber());
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
        queueTable.setRowHeight(32);
        queueTable.setFont(ModernTheme.FONT_REGULAR);
        queueTable.getTableHeader().setFont(ModernTheme.FONT_HEADER);
        queueTable.getTableHeader().setBackground(new Color(241, 245, 249));

        tableCard.add(new JScrollPane(queueTable), BorderLayout.CENTER);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JButton startConsultationBtn = ModernTheme.createPrimaryButton("Start Consultation");
        startConsultationBtn.addActionListener(e -> setStatus(AppointmentStatus.IN_CONSULTATION));

        JButton completeConsultationBtn = ModernTheme.createPrimaryButton("Complete Consultation & Notes");
        completeConsultationBtn.setBackground(ModernTheme.STATUS_COMPLETED);
        completeConsultationBtn.addActionListener(e -> showConsultationDialog());

        JButton issuePrescriptionBtn = ModernTheme.createPrimaryButton("Issue Prescription");
        issuePrescriptionBtn.setBackground(ModernTheme.ACCENT);
        issuePrescriptionBtn.addActionListener(e -> showPrescriptionDialog());

        toolbar.add(startConsultationBtn);
        toolbar.add(completeConsultationBtn);
        toolbar.add(issuePrescriptionBtn);

        tableCard.add(toolbar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
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

        JTextArea notesArea = new JTextArea(existingNotes != null ? existingNotes : "");
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

            if (med.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Medication name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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
}
