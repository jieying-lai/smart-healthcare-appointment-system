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
 * Dashboard panel for Doctors to manage patient consultations, record notes, and issue prescriptions.
 */
public class DoctorPanel extends JPanel {
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final Doctor doctor;

    private JTable queueTable;
    private DefaultTableModel queueTableModel;

    public DoctorPanel(AppointmentService appointmentService, PrescriptionService prescriptionService, Doctor doctor) {
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
        this.doctor = doctor;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshTable();
    }

    private void initUI() {
        JPanel toolbar = ModernTheme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JButton startConsultationBtn = ModernTheme.createPrimaryButton("Start Consultation");
        startConsultationBtn.addActionListener(e -> setStatus(AppointmentStatus.IN_CONSULTATION));

        JButton completeConsultationBtn = ModernTheme.createPrimaryButton("Complete Consultation & Notes");
        completeConsultationBtn.setBackground(ModernTheme.STATUS_COMPLETED);
        completeConsultationBtn.addActionListener(e -> showConsultationDialog());

        JButton issueRxBtn = ModernTheme.createSecondaryButton("Issue Prescription");
        issueRxBtn.addActionListener(e -> showPrescriptionDialog());

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> refreshTable());

        toolbar.add(startConsultationBtn);
        toolbar.add(completeConsultationBtn);
        toolbar.add(issueRxBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Patient ID", "Date", "Time", "Status", "Reason for Visit", "Consultation Notes"};
        queueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        queueTable = new JTable(queueTableModel);
        ModernTheme.styleTable(queueTable);

        add(new JScrollPane(queueTable), BorderLayout.CENTER);
    }

    public void refreshTable() {
        queueTableModel.setRowCount(0);
        List<Appointment> appts = appointmentService.getAppointmentsForUser(doctor);
        for (Appointment a : appts) {
            queueTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getPatientId(),
                a.getFormattedDate(),
                a.getFormattedTime(),
                a.getStatus(),
                a.getReasonForVisit(),
                a.getConsultationNotes()
            });
        }
    }

    private void setStatus(AppointmentStatus status) {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the queue.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        try {
            appointmentService.updateStatus(apptId, status);
            JOptionPane.showMessageDialog(this, "Status updated to: " + status.getLabel(), "Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        JTextArea notesArea = new JTextArea(existingNotes);
        notesArea.setFont(ModernTheme.FONT_REGULAR);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        JButton saveBtn = ModernTheme.createPrimaryButton("Save & Mark Completed");
        saveBtn.addActionListener(e -> {
            try {
                appointmentService.recordConsultation(apptId, notesArea.getText().trim());
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
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField medField = ModernTheme.createTextField();
        JTextField dosageField = ModernTheme.createTextField();
        JTextField instructionsField = ModernTheme.createTextField();

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medication Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(medField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Dosage (e.g. 500mg):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(dosageField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Instructions:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(instructionsField, gbc);

        JButton issueBtn = ModernTheme.createPrimaryButton("Send to Pharmacy");
        issueBtn.addActionListener(e -> {
            try {
                prescriptionService.createPrescription(
                    apptId, patId, patName, doctor.getUserId(), doctor.getFullName(), 
                    medField.getText().trim(), dosageField.getText().trim(), instructionsField.getText().trim()
                );
                JOptionPane.showMessageDialog(dialog, "Prescription issued to pharmacy!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(issueBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
