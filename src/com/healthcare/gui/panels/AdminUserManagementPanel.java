package com.healthcare.gui.panels;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.gui.ModernTheme;
import com.healthcare.model.*;
import com.healthcare.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Dashboard panel for Administrators to manage users, reset passwords, and toggle user active statuses.
 */
public class AdminUserManagementPanel extends JPanel {
    private final AuthService authService;

    private JTable userTable;
    private DefaultTableModel userTableModel;

    public AdminUserManagementPanel(AuthService authService) {
        this.authService = authService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshTable();
    }

    private void initUI() {
        JPanel toolbar = ModernTheme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        JButton addDoctorBtn = ModernTheme.createPrimaryButton("+ Add Doctor");
        addDoctorBtn.addActionListener(e -> showAddDoctorDialog());

        JButton addStaffBtn = ModernTheme.createSecondaryButton("+ Add Nurse/Pharmacist");
        addStaffBtn.addActionListener(e -> showAddStaffDialog());

        JButton toggleActiveBtn = ModernTheme.createSecondaryButton("Toggle Active/Deactive");
        toggleActiveBtn.addActionListener(e -> handleToggleActive());

        JButton resetPassBtn = ModernTheme.createDangerButton("Reset Password");
        resetPassBtn.addActionListener(e -> handleResetPassword());

        JButton refreshBtn = ModernTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTable());

        String[] cols = {"User ID", "Username", "Full Name", "Role", "Email", "Phone", "Status / Role Detail", "Active State"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userTableModel);
        ModernTheme.styleTable(userTable);

        JTextField searchField = ModernTheme.createSearchFilterField(userTable, userTableModel);
        searchField.setToolTipText("Search user by name, role, email or ID...");

        toolbar.add(addDoctorBtn);
        toolbar.add(addStaffBtn);
        toolbar.add(toggleActiveBtn);
        toolbar.add(resetPassBtn);
        toolbar.add(refreshBtn);
        toolbar.add(new JLabel("🔍 Filter:"));
        toolbar.add(searchField);

        add(toolbar, BorderLayout.NORTH);

        add(new JScrollPane(userTable), BorderLayout.CENTER);
    }

    public void refreshTable() {
        userTableModel.setRowCount(0);
        List<User> users = authService.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole().getDisplayName(),
                u.getEmail(),
                u.getPhone(),
                u.getRoleDescription(),
                u.isActive() ? "ACTIVE" : "DEACTIVATED"
            });
        }
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Doctor", true);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = ModernTheme.createTextField();
        JPasswordField passField = ModernTheme.createPasswordField();
        JTextField nameField = ModernTheme.createTextField();
        JTextField emailField = ModernTheme.createTextField();
        JTextField phoneField = ModernTheme.createTextField();
        JTextField specField = ModernTheme.createTextField();
        JTextField deptField = ModernTheme.createTextField();
        JTextField roomField = ModernTheme.createTextField();
        JTextField feeField = ModernTheme.createTextField();
        feeField.setText("150.0");

        int y = 0;
        addFormRow(panel, gbc, "Username:", userField, y++);
        addFormRow(panel, gbc, "Password:", passField, y++);
        addFormRow(panel, gbc, "Full Name:", nameField, y++);
        addFormRow(panel, gbc, "Email:", emailField, y++);
        addFormRow(panel, gbc, "Phone:", phoneField, y++);
        addFormRow(panel, gbc, "Specialization:", specField, y++);
        addFormRow(panel, gbc, "Department:", deptField, y++);
        addFormRow(panel, gbc, "Room Number:", roomField, y++);
        addFormRow(panel, gbc, "Consultation Fee (RM):", feeField, y++);

        JButton createBtn = ModernTheme.createPrimaryButton("Create Doctor Account");
        createBtn.addActionListener(e -> {
            try {
                double fee = 0.0;
                try {
                    fee = Double.parseDouble(feeField.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new InvalidRecordException("Consultation fee must be a valid numeric amount (e.g. 150.00).");
                }

                String uId = "USR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                Doctor doc = new Doctor(uId, userField.getText().trim(), new String(passField.getPassword()),
                                        nameField.getText().trim(), emailField.getText().trim(), 
                                        phoneField.getText().trim(), specField.getText().trim(), 
                                        deptField.getText().trim(), roomField.getText().trim(), 
                                        fee);
                authService.createUser(doc);
                JOptionPane.showMessageDialog(dialog, "Doctor account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Create Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(createBtn, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Staff Member", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Nurse", "Pharmacist"});
        JTextField userField = ModernTheme.createTextField();
        JPasswordField passField = ModernTheme.createPasswordField();
        JTextField nameField = ModernTheme.createTextField();
        JTextField emailField = ModernTheme.createTextField();
        JTextField phoneField = ModernTheme.createTextField();
        JTextField detailField = ModernTheme.createTextField();

        int y = 0;
        addFormRow(panel, gbc, "Role:", roleCombo, y++);
        addFormRow(panel, gbc, "Username:", userField, y++);
        addFormRow(panel, gbc, "Password:", passField, y++);
        addFormRow(panel, gbc, "Full Name:", nameField, y++);
        addFormRow(panel, gbc, "Email:", emailField, y++);
        addFormRow(panel, gbc, "Phone:", phoneField, y++);
        addFormRow(panel, gbc, "Dept/License Detail:", detailField, y++);

        JButton createBtn = ModernTheme.createPrimaryButton("Create Staff Account");
        createBtn.addActionListener(e -> {
            try {
                String uId = "USR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String selectedRole = (String) roleCombo.getSelectedItem();
                User user;
                if ("Nurse".equals(selectedRole)) {
                    user = new Nurse(uId, userField.getText().trim(), new String(passField.getPassword()),
                                     nameField.getText().trim(), emailField.getText().trim(), 
                                     phoneField.getText().trim(), detailField.getText().trim(), "Day Shift");
                } else {
                    user = new Pharmacist(uId, userField.getText().trim(), new String(passField.getPassword()),
                                          nameField.getText().trim(), emailField.getText().trim(), 
                                          phoneField.getText().trim(), detailField.getText().trim(), "Main Dispensary");
                }
                authService.createUser(user);
                JOptionPane.showMessageDialog(dialog, "Staff account created!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Create Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(createBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleToggleActive() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user account.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String uId = (String) userTableModel.getValueAt(row, 0);
        String currentState = (String) userTableModel.getValueAt(row, 7);
        boolean newActiveState = !"ACTIVE".equals(currentState);

        try {
            authService.toggleUserActiveStatus(uId, newActiveState);
            JOptionPane.showMessageDialog(this, "User status toggled to: " + (newActiveState ? "ACTIVE" : "DEACTIVATED"), "Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetPassword() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user account.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String uId = (String) userTableModel.getValueAt(row, 0);
        String username = (String) userTableModel.getValueAt(row, 1);

        String newPass = JOptionPane.showInputDialog(this, "Enter new password for '" + username + "':");
        if (newPass == null || newPass.trim().isEmpty()) return;

        try {
            authService.resetPassword(uId, newPass.trim());
            JOptionPane.showMessageDialog(this, "Password reset successfully for user: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (InvalidRecordException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component comp, int row) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row;
        panel.add(comp, gbc);
    }
}
