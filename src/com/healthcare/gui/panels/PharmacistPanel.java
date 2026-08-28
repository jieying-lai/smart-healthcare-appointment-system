package com.healthcare.gui.panels;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.gui.ModernTheme;
import com.healthcare.model.Pharmacist;
import com.healthcare.model.Prescription;
import com.healthcare.model.PrescriptionStatus;
import com.healthcare.service.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Dashboard panel for Pharmacists to manage prescription orders and medication dispensing.
 */
public class PharmacistPanel extends JPanel {
    private final PrescriptionService prescriptionService;
    private final Pharmacist pharmacist;

    private JTable rxTable;
    private DefaultTableModel rxTableModel;

    public PharmacistPanel(PrescriptionService prescriptionService, Pharmacist pharmacist) {
        this.prescriptionService = prescriptionService;
        this.pharmacist = pharmacist;

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

        JButton startPrepBtn = ModernTheme.createSecondaryButton("Mark PREPARING");
        startPrepBtn.addActionListener(e -> setStatus(PrescriptionStatus.PREPARING));

        JButton dispenseBtn = ModernTheme.createPrimaryButton("Dispense Medication");
        dispenseBtn.setBackground(ModernTheme.STATUS_COMPLETED);
        dispenseBtn.addActionListener(e -> setStatus(PrescriptionStatus.DISPENSED));

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh Prescriptions");
        refreshBtn.addActionListener(e -> refreshTable());

        toolbar.add(viewDetailsBtn);
        toolbar.add(startPrepBtn);
        toolbar.add(dispenseBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Prescription ID", "Patient Name", "Doctor Name", "Medication Name", "Dosage", "Instructions", "Status", "Issued Date"};
        rxTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        rxTable = new JTable(rxTableModel);
        ModernTheme.styleTable(rxTable);

        rxTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleViewDetails();
            }
        });

        add(new JScrollPane(rxTable), BorderLayout.CENTER);
    }

    private void handleViewDetails() {
        int row = rxTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a prescription from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rxId = (String) rxTableModel.getValueAt(row, 0);
        Prescription rx = prescriptionService.getAllPrescriptions().stream()
            .filter(r -> r.getPrescriptionId().equals(rxId)).findFirst().orElse(null);

        if (rx != null) {
            showFullDetailsDialog(rx);
        }
    }

    private void showFullDetailsDialog(Prescription rx) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Prescription Details - " + rx.getPrescriptionId(), true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addFormRow(panel, gbc, "Prescription ID:", new JLabel(rx.getPrescriptionId()), y++);
        addFormRow(panel, gbc, "Patient Name:", new JLabel(rx.getPatientName() + " (" + rx.getPatientId() + ")"), y++);
        addFormRow(panel, gbc, "Prescribing Doctor:", new JLabel(rx.getDoctorName() + " (" + rx.getDoctorId() + ")"), y++);
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
        rxTableModel.setRowCount(0);
        List<Prescription> rxs = prescriptionService.getAllPrescriptions();
        for (Prescription rx : rxs) {
            rxTableModel.addRow(new Object[]{
                rx.getPrescriptionId(),
                rx.getPatientName(),
                rx.getDoctorName(),
                rx.getMedicationName(),
                rx.getDosage(),
                rx.getInstructions(),
                rx.getStatus().getLabel(),
                rx.getFormattedIssuedAt()
            });
        }
    }

    private void setStatus(PrescriptionStatus status) {
        int row = rxTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a prescription from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rxId = (String) rxTableModel.getValueAt(row, 0);
        try {
            prescriptionService.updatePrescriptionStatus(rxId, status);
            JOptionPane.showMessageDialog(this, "Prescription status updated to: " + status.getLabel(), "Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
