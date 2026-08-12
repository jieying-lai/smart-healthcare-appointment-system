package com.healthcare.gui.panels;

import com.healthcare.gui.ModernTheme;
import com.healthcare.model.ReportData;
import com.healthcare.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * Analytical Report Generation panel for management and healthcare administrators.
 */
public class ReportPanel extends JPanel {
    private final ReportService reportService;
    private JTextArea reportTextArea;

    // Stat Cards Labels
    private JLabel totalApptsValue;
    private JLabel completedApptsValue;
    private JLabel totalRevenueValue;
    private JLabel totalPatientsValue;

    public ReportPanel(ReportService reportService) {
        this.reportService = reportService;

        setLayout(new BorderLayout(0, 16));
        setBackground(ModernTheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        refreshReport();
    }

    private void initUI() {
        // Top Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        totalApptsValue = new JLabel("0", SwingConstants.CENTER);
        completedApptsValue = new JLabel("0", SwingConstants.CENTER);
        totalRevenueValue = new JLabel("$0.00", SwingConstants.CENTER);
        totalPatientsValue = new JLabel("0", SwingConstants.CENTER);

        cardsGrid.add(createMetricCard("Total Bookings", totalApptsValue, ModernTheme.PRIMARY));
        cardsGrid.add(createMetricCard("Completed", completedApptsValue, ModernTheme.STATUS_COMPLETED));
        cardsGrid.add(createMetricCard("Total Revenue", totalRevenueValue, ModernTheme.ACCENT));
        cardsGrid.add(createMetricCard("Registered Patients", totalPatientsValue, ModernTheme.STATUS_SCHEDULED));

        add(cardsGrid, BorderLayout.NORTH);

        // Center Content: Formatted Text Report Output
        JPanel centerCard = ModernTheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 12));

        JPanel headerBox = new JPanel(new BorderLayout());
        headerBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Executive System Report & Statistics");
        titleLbl.setFont(ModernTheme.FONT_TITLE);

        JButton refreshBtn = ModernTheme.createPrimaryButton("Re-Generate Report");
        refreshBtn.addActionListener(e -> refreshReport());

        headerBox.add(titleLbl, BorderLayout.WEST);
        headerBox.add(refreshBtn, BorderLayout.EAST);

        centerCard.add(headerBox, BorderLayout.NORTH);

        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reportTextArea.setEditable(false);
        reportTextArea.setBackground(new Color(248, 250, 252));
        reportTextArea.setMargin(new Insets(12, 12, 12, 12));

        centerCard.add(new JScrollPane(reportTextArea), BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);
    }

    public void refreshReport() {
        ReportData data = reportService.generateSystemReport();
        totalApptsValue.setText(String.valueOf(data.getTotalAppointments()));
        completedApptsValue.setText(String.valueOf(data.getCompletedAppointments()));
        totalRevenueValue.setText("$" + String.format("%.2f", data.getTotalRevenue()));
        totalPatientsValue.setText(String.valueOf(data.getTotalPatients()));

        String summaryText = reportService.generateFormattedSummaryReport();
        reportTextArea.setText(summaryText);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ModernTheme.FONT_SUBTITLE);
        titleLbl.setForeground(ModernTheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
