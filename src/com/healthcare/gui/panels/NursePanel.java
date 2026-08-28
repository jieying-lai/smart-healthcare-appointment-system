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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        JButton viewDetailsBtn = ModernTheme.createSecondaryButton("View Full Details");
        viewDetailsBtn.addActionListener(e -> handleViewDetails());

        JButton checkInBtn = ModernTheme.createPrimaryButton("Check-In Patient (Mark WAITING)");
        checkInBtn.addActionListener(e -> setStatus(AppointmentStatus.WAITING));

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> refreshTable());

        toolbar.add(viewDetailsBtn);
        toolbar.add(checkInBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Doctor Name", "Date", "Time", "Status", "Reason for Visit"};
        queueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        queueTable = new JTable(queueTableModel);
        ModernTheme.styleTable(queueTable);

        queueTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        queueTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        queueTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        queueTable.getColumnModel().getColumn(6).setPreferredWidth(200);

        queueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        add(new JScrollPane(queueTable), BorderLayout.CENTER);
    }

    private void handleViewDetails() {
        int row = queueTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String apptId = (String) queueTableModel.getValueAt(row, 0);
        Appointment appt = appointmentService.getAllAppointments().stream()
            .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);

        if (appt != null) {
            showFullDetailsDialog(appt);
        }
    }

    private void showFullDetailsDialog(Appointment appt) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Patient Record Details - " + appt.getAppointmentId(), true);
        dialog.setSize(480, 380);
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

        JTextArea reasonArea = new JTextArea(appt.getReason());
        reasonArea.setEditable(false);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(ModernTheme.FONT_REGULAR);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Reason for Visit:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.gridwidth = 1; gbc.weightx = 0.7;
        panel.add(new JScrollPane(reasonArea), gbc);

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

    public void refreshTable() {
        queueTableModel.setRowCount(0);
        List<Appointment> appts = appointmentService.getAllAppointments();
        for (Appointment a : appts) {
            queueTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                a.getDoctorName(),
                a.getAppointmentDate().toString(),
                a.getAppointmentTime().toString(),
                a.getStatus().getLabel(),
                a.getReason()
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
