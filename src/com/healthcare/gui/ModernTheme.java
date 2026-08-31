package com.healthcare.gui;

import com.healthcare.model.AppointmentStatus;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI Design System providing Sky Blue & White design with custom painted buttons 
 * to guarantee 100% high contrast and visible text across all OS Look & Feels (Windows, Eclipse, macOS).
 */
public class ModernTheme {
    // Sky Blue & White Color Palette
    public static final Color PRIMARY = new Color(2, 132, 199);        // Rich Sky Blue (#0284C7)
    public static final Color PRIMARY_DARK = new Color(3, 105, 161);   // Deep Sky Blue (#0369A1)
    public static final Color PRIMARY_LIGHT = new Color(224, 242, 254); // Soft Ice Sky Blue (#E0F2FE)
    public static final Color ACCENT = new Color(14, 165, 233);        // Cyan Sky Blue (#0EA5E9)
    public static final Color BACKGROUND = new Color(240, 249, 255);   // Crisp Sky Tint Background (#F0F9FF)
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Charcoal Dark (#0F172A)
    public static final Color TEXT_MUTED = new Color(51, 65, 85);      // Steel Blue Grey (#334155)
    public static final Color BORDER = new Color(186, 230, 253);      // Light Sky Border (#BAE6FD)

    // Status Badge Colors
    public static final Color STATUS_SCHEDULED = new Color(2, 132, 199);    // Sky Blue
    public static final Color STATUS_WAITING = new Color(217, 119, 6);      // Amber Orange
    public static final Color STATUS_IN_CONSULTATION = new Color(124, 58, 237); // Deep Violet
    public static final Color STATUS_COMPLETED = new Color(5, 150, 105);    // Emerald Green
    public static final Color STATUS_CANCELLED = new Color(225, 29, 72);    // Rose Red

    // Typography
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Custom JButton subclass ensuring solid background fill & high contrast text 
     * regardless of native Windows/Eclipse Look & Feel overrides.
     */
    public static class CustomButton extends JButton {
        private Color normalBg;
        private Color hoverBg;

        public CustomButton(String text, Color normalBg, Color hoverBg, Color fgColor, Color borderColor) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            setFont(FONT_BOLD);
            setForeground(fgColor);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(8, 16, 8, 16)
            ));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(hoverBg.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JButton createPrimaryButton(String text) {
        return new CustomButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE, PRIMARY_DARK);
    }

    public static JButton createSecondaryButton(String text) {
        return new CustomButton(text, PRIMARY_LIGHT, new Color(186, 230, 253), PRIMARY_DARK, BORDER);
    }

    public static JButton createDangerButton(String text) {
        return new CustomButton(text, STATUS_CANCELLED, new Color(190, 18, 60), Color.WHITE, new Color(190, 18, 60));
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_DARK);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY_DARK);
        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_DARK);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY_DARK);
        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    public static JPanel createHeaderBanner(String titleText, String subtitleText, String roleBadgeText) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY); // Sky Blue Header Bar
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLbl = new JLabel(titleText);
        titleLbl.setFont(FONT_HEADER);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitleText);
        subLbl.setFont(FONT_REGULAR);
        subLbl.setForeground(PRIMARY_LIGHT);

        titleBox.add(titleLbl);
        titleBox.add(Box.createRigidArea(new Dimension(0, 4)));
        titleBox.add(subLbl);

        header.add(titleBox, BorderLayout.WEST);

        if (roleBadgeText != null) {
            JLabel badge = new JLabel("  " + roleBadgeText.toUpperCase() + "  ");
            badge.setFont(FONT_BOLD);
            badge.setForeground(PRIMARY_DARK);
            badge.setBackground(PRIMARY_LIGHT);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(6, 14, 6, 14));

            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
            badgeWrapper.setOpaque(false);
            badgeWrapper.add(badge);
            header.add(badgeWrapper, BorderLayout.EAST);
        }

        return header;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);

        // Header Styling: High Contrast Sky Blue with Crisp White Text
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setOpaque(false);

        // Custom Cell Renderer for High Contrast & Status Badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                c.setOpaque(true);

                String strVal = val != null ? val.toString() : "";
                c.setOpaque(true);

                if (strVal.equals("Scheduled") || strVal.equals("SCHEDULED")) {
                    c.setForeground(STATUS_SCHEDULED);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("Waiting in Queue") || strVal.equals("WAITING")) {
                    c.setForeground(STATUS_WAITING);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("In Consultation") || strVal.equals("IN_CONSULTATION")) {
                    c.setForeground(STATUS_IN_CONSULTATION);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("Completed") || strVal.equals("COMPLETED") || strVal.equals("Dispensed") || strVal.equals("DISPENSED")) {
                    c.setForeground(STATUS_COMPLETED);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("Cancelled") || strVal.equals("CANCELLED")) {
                    c.setForeground(STATUS_CANCELLED);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("Pending (Pharmacy)") || strVal.equals("PENDING")) {
                    c.setForeground(STATUS_WAITING);
                    c.setFont(FONT_BOLD);
                } else if (strVal.equals("Preparing") || strVal.equals("PREPARING")) {
                    c.setForeground(STATUS_IN_CONSULTATION);
                    c.setFont(FONT_BOLD);
                } else {
                    c.setFont(FONT_REGULAR);
                    c.setForeground(isSelected ? PRIMARY_DARK : TEXT_DARK);
                }

                if (isSelected) {
                    c.setBackground(PRIMARY_LIGHT);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return c;
            }
        });
    }

    public static JTextField createSearchFilterField(JTable table, javax.swing.table.DefaultTableModel model) {
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField searchField = createTextField();
        searchField.setPreferredSize(new Dimension(240, 32));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        return searchField;
    }
}
