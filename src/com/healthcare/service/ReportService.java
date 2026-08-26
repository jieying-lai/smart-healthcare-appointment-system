package com.healthcare.service;

import com.healthcare.model.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for analytical report generation and statistics calculation.
 */
public class ReportService {
    private final DataStorageService dataStorage;

    public ReportService(DataStorageService dataStorage) {
        this.dataStorage = dataStorage;
    }

    public ReportData generateSystemReport() {
        Collection<Appointment> appts = dataStorage.getAppointments().values();
        Collection<User> users = dataStorage.getUsers().values();

        int totalAppts = appts.size();
        int completed = 0;
        int cancelled = 0;
        double totalRevenue = 0.0;

        Map<String, Integer> statusBreakdown = new HashMap<>();
        for (AppointmentStatus s : AppointmentStatus.values()) {
            statusBreakdown.put(s.getLabel(), 0);
        }

        Map<String, Integer> doctorWorkload = new HashMap<>();

        for (Appointment a : appts) {
            String statusLabel = a.getStatus().getLabel();
            statusBreakdown.put(statusLabel, statusBreakdown.getOrDefault(statusLabel, 0) + 1);

            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                completed++;
                totalRevenue += a.getFee();
            } else if (a.getStatus() == AppointmentStatus.CANCELLED) {
                cancelled++;
            }

            String docName = a.getDoctorName();
            doctorWorkload.put(docName, doctorWorkload.getOrDefault(docName, 0) + 1);
        }

        int totalPatients = 0;
        int totalDoctors = 0;
        for (User u : users) {
            if (u instanceof Patient) totalPatients++;
            if (u instanceof Doctor) totalDoctors++;
        }

        return new ReportData(totalAppts, completed, cancelled, totalPatients, totalDoctors, 
                               totalRevenue, statusBreakdown, doctorWorkload);
    }

    public String generateFormattedSummaryReport() {
        ReportData data = generateSystemReport();
        StringBuilder sb = new StringBuilder();

        sb.append("===============================================================\n");
        sb.append("      SMART HEALTHCARE SYSTEM - EXECUTIVE MANAGEMENT REPORT    \n");
        sb.append("      Generated Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append("===============================================================\n\n");

        sb.append("1. SYSTEM OVERVIEW & CAPACITY\n");
        sb.append("   - Total Registered Patients : ").append(data.getTotalPatients()).append("\n");
        sb.append("   - Total Active Doctors     : ").append(data.getTotalDoctors()).append("\n");
        sb.append("   - Total Appointments       : ").append(data.getTotalAppointments()).append("\n");
        sb.append("   - Total Revenue Generated  : RM ").append(String.format("%.2f", data.getTotalRevenue())).append("\n\n");

        sb.append("2. APPOINTMENT STATUS BREAKDOWN\n");
        for (Map.Entry<String, Integer> entry : data.getStatusBreakdown().entrySet()) {
            sb.append("   - ").append(String.format("%-18s", entry.getKey())).append(" : ").append(entry.getValue()).append("\n");
        }
        sb.append("\n");

        sb.append("3. DOCTOR CONSULTATION WORKLOAD SUMMARY\n");
        for (Map.Entry<String, Integer> entry : data.getDoctorWorkload().entrySet()) {
            sb.append("   - ").append(String.format("%-25s", entry.getKey())).append(" : ").append(entry.getValue()).append(" appointment(s)\n");
        }
        sb.append("\n===============================================================\n");

        return sb.toString();
    }
}
