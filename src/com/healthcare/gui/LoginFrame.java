package com.healthcare.gui;

import com.healthcare.exception.AuthenticationException;
import com.healthcare.exception.InvalidRecordException;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.AuthService;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Login and Patient Registration Frame with Quick Account Shortcuts.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final Runnable onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(AuthService authService, Runnable onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setTitle("Smart Healthcare System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);

        // Header
        JPanel header = ModernTheme.createHeaderBanner(
            "Smart Healthcare Management System", 
            "Appointment & Consultation Management System", 
            "Portal Access"
        );
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Content split into Login Box & Quick Login Presets
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        contentPanel.setOpaque(false);

        // Left Box: Login Form Card
        JPanel loginCard = ModernTheme.createCardPanel();
        loginCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLbl = new JLabel("System Login");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        titleLbl.setForeground(ModernTheme.PRIMARY_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginCard.add(titleLbl, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(ModernTheme.FONT_BOLD);
        loginCard.add(userLbl, gbc);

        usernameField = ModernTheme.createTextField();
        gbc.gridx = 1; gbc.gridy = 1;
        loginCard.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(ModernTheme.FONT_BOLD);
        loginCard.add(passLbl, gbc);

        passwordField = ModernTheme.createPasswordField();
        gbc.gridx = 1; gbc.gridy = 2;
        loginCard.add(passwordField, gbc);

        // Buttons
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        JButton registerBtn = ModernTheme.createSecondaryButton("Register Patient");
        registerBtn.addActionListener(e -> showPatientRegistrationDialog());

        JButton loginBtn = ModernTheme.createPrimaryButton("Login");
        loginBtn.addActionListener(e -> handleLogin());

        btnBox.add(registerBtn);
        btnBox.add(loginBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);
        loginCard.add(btnBox, gbc);

        contentPanel.add(loginCard);

        // Right Box: Quick Demo Login Accounts Card
        JPanel quickLoginCard = ModernTheme.createCardPanel();
        quickLoginCard.setLayout(new BorderLayout(0, 10));

        JLabel demoHeader = new JLabel("Quick Demo Logins (Click to Auto-fill)");
        demoHeader.setFont(ModernTheme.FONT_TITLE);
        demoHeader.setForeground(ModernTheme.TEXT_DARK);
        quickLoginCard.add(demoHeader, BorderLayout.NORTH);

        JPanel accountsList = new JPanel();
        accountsList.setLayout(new BoxLayout(accountsList, BoxLayout.Y_AXIS));
        accountsList.setOpaque(false);

        addQuickLoginButton(accountsList, "Lai Jie Ying (Admin)", "admin", "admin123", Role.ADMIN);
        addQuickLoginButton(accountsList, "Dr. Ahmad Razali (Doctor)", "dr_razali", "doc123", Role.DOCTOR);
        addQuickLoginButton(accountsList, "Nurse Siti Nurhaliza (Nurse)", "nurse_siti", "nurse123", Role.NURSE);
        addQuickLoginButton(accountsList, "Wong Wei Jun (Pharmacist)", "pharm_wong", "pharm123", Role.PHARMACIST);
        addQuickLoginButton(accountsList, "Tan Ah Hock (Patient)", "tan_ah_hock", "pass123", Role.PATIENT);

        JScrollPane scroll = new JScrollPane(accountsList);
        scroll.setBorder(null);
        quickLoginCard.add(scroll, BorderLayout.CENTER);

        contentPanel.add(quickLoginCard);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addQuickLoginButton(JPanel parent, String labelText, String user, String pass, Role role) {
        JButton btn = new JButton("<html><body style='color:#0F172A;'><b style='color:#0369A1;font-size:11px;'>" + labelText + "</b><br><span style='color:#334155;font-size:10px;'>Username: <b>" + user + "</b> &nbsp;|&nbsp; Password: <b>" + pass + "</b></span></body></html>");
        btn.setFont(ModernTheme.FONT_REGULAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(ModernTheme.PRIMARY_LIGHT);
        btn.setForeground(ModernTheme.TEXT_DARK);
        btn.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(186, 230, 253));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ModernTheme.PRIMARY_LIGHT);
            }
        });

        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            handleLogin();
        });

        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private void handleLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        try {
            User loggedIn = authService.login(user, pass);
            JOptionPane.showMessageDialog(this, "Welcome, " + loggedIn.getFullName() + "!\nRole: " + loggedIn.getRole().getDisplayName(), 
                                          "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            onLoginSuccess.run();
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPatientRegistrationDialog() {
        JDialog dialog = new JDialog(this, "New Patient Registration", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField regUser = ModernTheme.createTextField();
        JPasswordField regPass = ModernTheme.createPasswordField();
        JTextField regName = ModernTheme.createTextField();
        JTextField regEmail = ModernTheme.createTextField();
        JTextField regPhone = ModernTheme.createTextField();
        JTextField regDob = ModernTheme.createTextField();
        regDob.setText("1995-08-15");
        JComboBox<String> regBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JTextField regHistory = ModernTheme.createTextField();
        JTextField regEmergency = ModernTheme.createTextField();

        int y = 0;
        addFormRow(panel, gbc, "Username:", regUser, y++);
        addFormRow(panel, gbc, "Password:", regPass, y++);
        addFormRow(panel, gbc, "Full Name:", regName, y++);
        addFormRow(panel, gbc, "Email:", regEmail, y++);
        addFormRow(panel, gbc, "Phone:", regPhone, y++);
        addFormRow(panel, gbc, "Date of Birth (YYYY-MM-DD):", regDob, y++);
        addFormRow(panel, gbc, "Blood Group:", regBlood, y++);
        addFormRow(panel, gbc, "Medical History:", regHistory, y++);
        addFormRow(panel, gbc, "Emergency Contact:", regEmergency, y++);

        JButton submitBtn = ModernTheme.createPrimaryButton("Submit Registration");
        submitBtn.addActionListener(e -> {
            try {
                authService.registerPatient(
                    regUser.getText().trim(),
                    new String(regPass.getPassword()),
                    regName.getText().trim(),
                    regEmail.getText().trim(),
                    regPhone.getText().trim(),
                    regDob.getText().trim(),
                    (String) regBlood.getSelectedItem(),
                    regHistory.getText().trim(),
                    regEmergency.getText().trim()
                );
                JOptionPane.showMessageDialog(dialog, "Registration successful! You may now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                usernameField.setText(regUser.getText().trim());
                passwordField.setText(new String(regPass.getPassword()));
            } catch (InvalidRecordException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(submitBtn, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
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
