package com.healthcare.service;

import com.healthcare.model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing in-memory state and object file persistence.
 * Initializes realistic sample demo records if no data file is found.
 */
public class DataStorageService {
    private static final String DATA_FILE = "data/system_store.dat";

    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Appointment> appointments = new ConcurrentHashMap<>();
    private Map<String, Prescription> prescriptions = new ConcurrentHashMap<>();
    private List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());

    public DataStorageService() {
        loadData();
        if (users.isEmpty()) {
            initSampleData();
            saveData();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
            appointments = (Map<String, Appointment>) ois.readObject();
            prescriptions = (Map<String, Prescription>) ois.readObject();
            notifications = (List<Notification>) ois.readObject();
            System.out.println("Data successfully loaded from persistent storage.");
        } catch (Exception e) {
            System.err.println("Notice: Persistent data file missing or incompatible. Re-initializing sample dataset.");
            users.clear();
            appointments.clear();
            prescriptions.clear();
            notifications.clear();
        }
    }

    public synchronized void saveData() {
        try {
            File dir = new File("data");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(users);
                oos.writeObject(appointments);
                oos.writeObject(prescriptions);
                oos.writeObject(notifications);
            }
        } catch (IOException e) {
            System.err.println("Error saving system data: " + e.getMessage());
        }
    }

    private void initSampleData() {
        System.out.println("Initializing default healthcare sample accounts and records...");

        // 1. Admin
        Admin admin = new Admin("USR-001", "admin", "admin123", "System Administrator", 
                                "admin@healthsystem.com", "+1-555-0100", "Level 5 Full Access");
        users.put(admin.getUserId(), admin);

        // 2. Doctors
        Doctor doc1 = new Doctor("USR-002", "dr_smith", "doc123", "Dr. Alexander Smith", 
                                 "asmith@healthsystem.com", "+1-555-0101", "Cardiology", 
                                 "Cardiovascular Sciences", "Room 302", 150.00);
        Doctor doc2 = new Doctor("USR-003", "dr_wong", "doc123", "Dr. Emily Wong", 
                                 "ewong@healthsystem.com", "+1-555-0102", "Dermatology", 
                                 "Dermatology & Skin Care", "Room 105", 120.00);
        Doctor doc3 = new Doctor("USR-004", "dr_patel", "doc123", "Dr. Rajesh Patel", 
                                 "rpatel@healthsystem.com", "+1-555-0103", "Pediatrics", 
                                 "Child Healthcare", "Room 210", 110.00);
        users.put(doc1.getUserId(), doc1);
        users.put(doc2.getUserId(), doc2);
        users.put(doc3.getUserId(), doc3);

        // 3. Nurse
        Nurse nurse1 = new Nurse("USR-005", "nurse_joy", "nurse123", "Nurse Joy Miller", 
                                 "jmiller@healthsystem.com", "+1-555-0104", "Outpatient Care", "Morning (08:00 - 16:00)");
        users.put(nurse1.getUserId(), nurse1);

        // 4. Pharmacist
        Pharmacist pharm1 = new Pharmacist("USR-006", "pharm_claire", "pharm123", "Claire Redfield", 
                                           "credfield@healthsystem.com", "+1-555-0105", "PH-994821", "Main Dispensary");
        users.put(pharm1.getUserId(), pharm1);

        // 5. Patients
        Patient pat1 = new Patient("USR-007", "john_doe", "pass123", "John Doe", 
                                   "jdoe@example.com", "+1-555-0106", "1988-05-14", "O+", 
                                   "Hypertension, Allergy to Penicillin", "+1-555-0199");
        Patient pat2 = new Patient("USR-008", "sarah_connor", "pass123", "Sarah Connor", 
                                   "sconnor@example.com", "+1-555-0107", "1992-11-20", "A-", 
                                   "No major past illnesses", "+1-555-0198");
        users.put(pat1.getUserId(), pat1);
        users.put(pat2.getUserId(), pat2);

        // Sample Appointments
        LocalDate today = LocalDate.now();
        Appointment app1 = new Appointment("APT-1001", pat1.getUserId(), pat1.getFullName(), 
                                            doc1.getUserId(), doc1.getFullName(), today, 
                                            LocalTime.of(9, 30), "Annual Cardiovascular Checkup", doc1.getConsultationFee());
        app1.setStatus(AppointmentStatus.SCHEDULED);

        Appointment app2 = new Appointment("APT-1002", pat2.getUserId(), pat2.getFullName(), 
                                            doc2.getUserId(), doc2.getFullName(), today, 
                                            LocalTime.of(10, 30), "Skin Rash Inspection", doc2.getConsultationFee());
        app2.setStatus(AppointmentStatus.WAITING);

        Appointment app3 = new Appointment("APT-1003", pat1.getUserId(), pat1.getFullName(), 
                                            doc3.getUserId(), doc3.getFullName(), today.minusDays(1), 
                                            LocalTime.of(14, 0), "Routine Health Assessment", doc3.getConsultationFee());
        app3.setStatus(AppointmentStatus.COMPLETED);
        app3.setConsultationNotes("Patient in healthy state. Prescribed Vitamin D supplements.");

        appointments.put(app1.getAppointmentId(), app1);
        appointments.put(app2.getAppointmentId(), app2);
        appointments.put(app3.getAppointmentId(), app3);

        // Sample Prescription
        Prescription rx1 = new Prescription("RX-5001", app3.getAppointmentId(), pat1.getUserId(), 
                                            pat1.getFullName(), doc3.getUserId(), doc3.getFullName(), 
                                            "Vitamin D3 1000 IU", "1 tablet daily", "Take after breakfast with water");
        prescriptions.put(rx1.getPrescriptionId(), rx1);

        // Sample Notifications
        notifications.add(new Notification("NOTIF-101", pat1.getUserId(), "Appointment Confirmed", 
                                          "Your appointment with Dr. Alexander Smith is scheduled for today at 09:30.", "APPOINTMENT"));
        notifications.add(new Notification("NOTIF-102", pat2.getUserId(), "Queue Update", 
                                          "You have been checked in by Nurse Joy Miller. Please proceed to Waiting Area B.", "STATUS_CHANGE"));
    }

    // Accessors
    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Appointment> getAppointments() {
        return appointments;
    }

    public Map<String, Prescription> getPrescriptions() {
        return prescriptions;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
