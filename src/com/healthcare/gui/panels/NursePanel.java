package com.healthcare.gui.panels;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.gui.ModernTheme;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.model.Nurse;
import com.healthcare.service.AppointmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel for Nurses to manage patient check-in and active waiting queue.
 */
public class NursePanel extends JPanel {
    private final AppointmentService appointmentService;
    private final Nurse nurse;

    private JTable queueTable;
    private DefaultTableModel queueTableModel;

    public NursePanel(AppointmentService appointmentService, Nurse nurse) {
        this.appointmentService = appointmentService;
        this.nurse = nurse;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshTable();
    }

    private void initUI() {
        JPanel toolbar = ModernTheme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JButton checkInBtn = ModernTheme.createPrimaryButton("Check-In Patient (Mark WAITING)");
        checkInBtn.addActionListener(e -> setStatus(AppointmentStatus.WAITING));

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> refreshTable());

        toolbar.add(checkInBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Doctor Name", "Date", "Time", "Status", "Reason"};
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
        List<Appointment> appts = appointmentService.getAllAppointments();
        for (Appointment a : appts) {
            queueTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getDoctorName(),
                a.getFormattedDate(),
                a.getFormattedTime(),
                a.getStatus(),
                a.getReasonForVisit()
            });
        }
    }

    private void setStatus(AppointmentStatus status) {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        try {
            appointmentService.updateStatus(apptId, status);
            JOptionPane.showMessageDialog(this, "Patient checked in! Status changed to WAITING.", "Check-In Complete", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
