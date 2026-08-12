package com.healthcare.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Container model for analytics and generated reports.
 */
public class ReportData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private int totalPatients;
    private int totalDoctors;
    private double totalRevenue;
    private Map<String, Integer> statusBreakdown;
    private Map<String, Integer> doctorWorkload;

    public ReportData(int totalAppointments, int completedAppointments, int cancelledAppointments, 
                      int totalPatients, int totalDoctors, double totalRevenue, 
                      Map<String, Integer> statusBreakdown, Map<String, Integer> doctorWorkload) {
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalRevenue = totalRevenue;
        this.statusBreakdown = statusBreakdown;
        this.doctorWorkload = doctorWorkload;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public int getCompletedAppointments() {
        return completedAppointments;
    }

    public int getCancelledAppointments() {
        return cancelledAppointments;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public int getTotalDoctors() {
        return totalDoctors;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public Map<String, Integer> getStatusBreakdown() {
        return statusBreakdown;
    }

    public Map<String, Integer> getDoctorWorkload() {
        return doctorWorkload;
    }
}
