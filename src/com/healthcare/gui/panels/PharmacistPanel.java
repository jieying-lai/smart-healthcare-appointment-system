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

        JButton startPrepBtn = ModernTheme.createSecondaryButton("Mark PREPARING");
        startPrepBtn.addActionListener(e -> setStatus(PrescriptionStatus.PREPARING));

        JButton dispenseBtn = ModernTheme.createPrimaryButton("Dispense Medication");
        dispenseBtn.setBackground(ModernTheme.STATUS_COMPLETED);
        dispenseBtn.addActionListener(e -> setStatus(PrescriptionStatus.DISPENSED));

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh Prescriptions");
        refreshBtn.addActionListener(e -> refreshTable());

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

        add(new JScrollPane(rxTable), BorderLayout.CENTER);
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
