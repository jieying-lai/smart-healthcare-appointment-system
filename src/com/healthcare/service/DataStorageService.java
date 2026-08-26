package com.healthcare.service;

import com.healthcare.model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing in-memory state and plain-text file persistence (.txt files).
 * Uses realistic Malaysian healthcare sample accounts and RM currency format.
 */
public class DataStorageService {
    private static final String USERS_FILE = "data/users.txt";
    private static final String APPOINTMENTS_FILE = "data/appointments.txt";
    private static final String PRESCRIPTIONS_FILE = "data/prescriptions.txt";
    private static final String NOTIFICATIONS_FILE = "data/notifications.txt";

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

    public synchronized void loadData() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            return;
        }

        loadUsersText();
        loadAppointmentsText();
        loadPrescriptionsText();
        loadNotificationsText();
    }

    public synchronized void saveData() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        saveUsersText();
        saveAppointmentsText();
        savePrescriptionsText();
        saveNotificationsText();
    }

    private void loadUsersText() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue;

                String type = parts[0];
                String userId = parts[1];
                String username = parts[2];
                String password = parts[3];
                String fullName = parts[4];
                String email = parts[5];
                String phone = parts[6];

                if ("ADMIN".equals(type) && parts.length >= 9) {
                    Admin admin = new Admin(userId, username, password, fullName, email, phone, parts[7]);
                    admin.setActive(Boolean.parseBoolean(parts[8]));
                    users.put(userId, admin);
                } else if ("DOCTOR".equals(type) && parts.length >= 12) {
                    Doctor doc = new Doctor(userId, username, password, fullName, email, phone, 
                                            parts[7], parts[8], parts[9], Double.parseDouble(parts[10]));
                    doc.setActive(Boolean.parseBoolean(parts[11]));
                    users.put(userId, doc);
                } else if ("NURSE".equals(type) && parts.length >= 10) {
                    Nurse nurse = new Nurse(userId, username, password, fullName, email, phone, parts[7], parts[8]);
                    nurse.setActive(Boolean.parseBoolean(parts[9]));
                    users.put(userId, nurse);
                } else if ("PHARMACIST".equals(type) && parts.length >= 10) {
                    Pharmacist pharm = new Pharmacist(userId, username, password, fullName, email, phone, parts[7], parts[8]);
                    pharm.setActive(Boolean.parseBoolean(parts[9]));
                    users.put(userId, pharm);
                } else if ("PATIENT".equals(type) && parts.length >= 12) {
                    Patient pat = new Patient(userId, username, password, fullName, email, phone, 
                                              parts[7], parts[8], parts[9], parts[10]);
                    pat.setActive(Boolean.parseBoolean(parts[11]));
                    users.put(userId, pat);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users text file: " + e.getMessage());
        }
    }

    private void saveUsersText() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println("# User Data Storage: TYPE|userId|username|password|fullName|email|phone|extraFields...|active");
            for (User u : users.values()) {
                if (u instanceof Admin) {
                    Admin a = (Admin) u;
                    writer.println("ADMIN|" + a.getUserId() + "|" + a.getUsername() + "|" + a.getPassword() + "|" + 
                                   a.getFullName() + "|" + a.getEmail() + "|" + a.getPhone() + "|" + 
                                   a.getAccessLevel() + "|" + a.isActive());
                } else if (u instanceof Doctor) {
                    Doctor d = (Doctor) u;
                    writer.println("DOCTOR|" + d.getUserId() + "|" + d.getUsername() + "|" + d.getPassword() + "|" + 
                                   d.getFullName() + "|" + d.getEmail() + "|" + d.getPhone() + "|" + 
                                   d.getSpecialization() + "|" + d.getDepartment() + "|" + d.getRoomNumber() + "|" + 
                                   d.getConsultationFee() + "|" + d.isActive());
                } else if (u instanceof Nurse) {
                    Nurse n = (Nurse) u;
                    writer.println("NURSE|" + n.getUserId() + "|" + n.getUsername() + "|" + n.getPassword() + "|" + 
                                   n.getFullName() + "|" + n.getEmail() + "|" + n.getPhone() + "|" + 
                                   n.getDepartment() + "|" + n.getShiftInfo() + "|" + n.isActive());
                } else if (u instanceof Pharmacist) {
                    Pharmacist p = (Pharmacist) u;
                    writer.println("PHARMACIST|" + p.getUserId() + "|" + p.getUsername() + "|" + p.getPassword() + "|" + 
                                   p.getFullName() + "|" + p.getEmail() + "|" + p.getPhone() + "|" + 
                                   p.getLicenseNumber() + "|" + p.getPharmacySection() + "|" + p.isActive());
                } else if (u instanceof Patient) {
                    Patient pat = (Patient) u;
                    writer.println("PATIENT|" + pat.getUserId() + "|" + pat.getUsername() + "|" + pat.getPassword() + "|" + 
                                   pat.getFullName() + "|" + pat.getEmail() + "|" + pat.getPhone() + "|" + 
                                   pat.getDateOfBirth() + "|" + pat.getBloodGroup() + "|" + pat.getMedicalHistory() + "|" + 
                                   pat.getEmergencyContact() + "|" + pat.isActive());
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving users text file: " + e.getMessage());
        }
    }

    private void loadAppointmentsText() {
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 11) {
                    Appointment appt = new Appointment(
                        parts[0], parts[1], parts[2], parts[3], parts[4], 
                        LocalDate.parse(parts[5]), LocalTime.parse(parts[6]), 
                        parts[8], Double.parseDouble(parts[10])
                    );
                    appt.setStatus(AppointmentStatus.valueOf(parts[7]));
                    appt.setConsultationNotes(parts[9]);
                    appointments.put(appt.getAppointmentId(), appt);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading appointments text file: " + e.getMessage());
        }
    }

    private void saveAppointmentsText() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            writer.println("# Appointment Data Storage: apptId|patientId|patientName|doctorId|doctorName|date|time|status|reason|notes|fee");
            for (Appointment a : appointments.values()) {
                writer.println(a.getAppointmentId() + "|" + a.getPatientId() + "|" + a.getPatientName() + "|" + 
                               a.getDoctorId() + "|" + a.getDoctorName() + "|" + a.getAppointmentDate() + "|" + 
                               a.getAppointmentTime() + "|" + a.getStatus().name() + "|" + a.getReasonForVisit() + "|" + 
                               a.getConsultationNotes() + "|" + a.getFee());
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments text file: " + e.getMessage());
        }
    }

    private void loadPrescriptionsText() {
        File file = new File(PRESCRIPTIONS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 10) {
                    Prescription rx = new Prescription(
                        parts[0], parts[1], parts[2], parts[3], parts[4], 
                        parts[5], parts[6], parts[7], parts[8]
                    );
                    rx.setStatus(PrescriptionStatus.valueOf(parts[9]));
                    prescriptions.put(rx.getPrescriptionId(), rx);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading prescriptions text file: " + e.getMessage());
        }
    }

    private void savePrescriptionsText() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRESCRIPTIONS_FILE))) {
            writer.println("# Prescription Data Storage: rxId|apptId|patientId|patientName|doctorId|doctorName|medication|dosage|instructions|status");
            for (Prescription rx : prescriptions.values()) {
                writer.println(rx.getPrescriptionId() + "|" + rx.getAppointmentId() + "|" + rx.getPatientId() + "|" + 
                               rx.getPatientName() + "|" + rx.getDoctorId() + "|" + rx.getDoctorName() + "|" + 
                               rx.getMedicationName() + "|" + rx.getDosage() + "|" + rx.getInstructions() + "|" + 
                               rx.getStatus().name());
            }
        } catch (IOException e) {
            System.err.println("Error saving prescriptions text file: " + e.getMessage());
        }
    }

    private void loadNotificationsText() {
        File file = new File(NOTIFICATIONS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    Notification notif = new Notification(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    notif.setRead(Boolean.parseBoolean(parts[5]));
                    notifications.add(notif);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading notifications text file: " + e.getMessage());
        }
    }

    private void saveNotificationsText() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOTIFICATIONS_FILE))) {
            writer.println("# Notification Data Storage: notifId|recipientId|title|message|type|isRead");
            for (Notification n : notifications) {
                writer.println(n.getNotificationId() + "|" + n.getRecipientUserId() + "|" + n.getTitle() + "|" + 
                               n.getMessage() + "|" + n.getType() + "|" + n.isRead());
            }
        } catch (IOException e) {
            System.err.println("Error saving notifications text file: " + e.getMessage());
        }
    }

    private void initSampleData() {
        System.out.println("Initializing default Malaysian healthcare sample accounts and records...");

        // 1. Admin (Malaysian)
        Admin admin = new Admin("USR-001", "admin", "admin123", "Tan Jin Heng (Admin Alex)", 
                                "alex.tan@pantai.com.my", "+60 12-345 6789", "Level 5 Super Admin");
        users.put(admin.getUserId(), admin);

        // 2. Doctors (Malaysian - Malay, Chinese, Indian)
        Doctor doc1 = new Doctor("USR-002", "dr_razali", "doc123", "Dr. Ahmad Razali bin Hassan", 
                                 "razali@pantai.com.my", "+60 19-223 4567", "Kardiologi (Cardiology)", 
                                 "Pusat Jantung Negara", "Bilik 302", 150.00);
        Doctor doc2 = new Doctor("USR-003", "dr_lee", "doc123", "Dr. Lee Mei Ling", 
                                 "meiling@pantai.com.my", "+60 16-334 5678", "Dermatologi (Skin Care)", 
                                 "Jabatan Kulit", "Bilik 105", 120.00);
        Doctor doc3 = new Doctor("USR-004", "dr_kavitha", "doc123", "Dr. Kavitha a/p Subramaniam", 
                                 "kavitha@pantai.com.my", "+60 17-445 6789", "Pediatrik (Child Health)", 
                                 "Jabatan Kanak-Kanak", "Bilik 210", 110.00);
        users.put(doc1.getUserId(), doc1);
        users.put(doc2.getUserId(), doc2);
        users.put(doc3.getUserId(), doc3);

        // 3. Nurse (Malaysian)
        Nurse nurse1 = new Nurse("USR-005", "nurse_siti", "nurse123", "Nurse Siti Nurhaliza binti Ali", 
                                 "snurhaliza@pantai.com.my", "+60 13-556 7890", "Jabatan Pesakit Luar", "Syif Pagi (08:00 - 16:00)");
        users.put(nurse1.getUserId(), nurse1);

        // 4. Pharmacist (Malaysian)
        Pharmacist pharm1 = new Pharmacist("USR-006", "pharm_wong", "pharm123", "Wong Wei Jun (Pharmacist)", 
                                           "weijun@pantai.com.my", "+60 14-667 8901", "MPS-994821", "Farmasi Utama");
        users.put(pharm1.getUserId(), pharm1);

        // 5. Patients (Malaysian)
        Patient pat1 = new Patient("USR-007", "tan_ah_hock", "pass123", "Tan Ah Hock", 
                                   "tahock@gmail.com", "+60 12-778 9012", "1988-05-14", "O+", 
                                   "Darah Tinggi, Alahan Penicillin", "+60 12-999 8877");
        Patient pat2 = new Patient("USR-008", "nur_aisyah", "pass123", "Nur Aisyah binti Ismail", 
                                   "aisyah@yahoo.com.my", "+60 18-889 0123", "1995-11-20", "A-", 
                                   "Tiada rekod penyakit kronik", "+60 11-2233 4455");
        users.put(pat1.getUserId(), pat1);
        users.put(pat2.getUserId(), pat2);

        // Sample Appointments (Malaysian)
        LocalDate today = LocalDate.now();
        Appointment app1 = new Appointment("APT-1001", pat1.getUserId(), pat1.getFullName(), 
                                            doc1.getUserId(), doc1.getFullName(), today, 
                                            LocalTime.of(9, 30), "Pemeriksaan Kardiologi Tahunan", doc1.getConsultationFee());
        app1.setStatus(AppointmentStatus.SCHEDULED);

        Appointment app2 = new Appointment("APT-1002", pat2.getUserId(), pat2.getFullName(), 
                                            doc2.getUserId(), doc2.getFullName(), today, 
                                            LocalTime.of(10, 30), "Rawatan Masalah Kulit", doc2.getConsultationFee());
        app2.setStatus(AppointmentStatus.WAITING);

        Appointment app3 = new Appointment("APT-1003", pat1.getUserId(), pat1.getFullName(), 
                                            doc3.getUserId(), doc3.getFullName(), today.minusDays(1), 
                                            LocalTime.of(14, 0), "Pemeriksaan Kesihatan Rutin", doc3.getConsultationFee());
        app3.setStatus(AppointmentStatus.COMPLETED);
        app3.setConsultationNotes("Pesakit dalam keadaan sihat. Dipreskripsi Ubat Vitamin D3.");

        appointments.put(app1.getAppointmentId(), app1);
        appointments.put(app2.getAppointmentId(), app2);
        appointments.put(app3.getAppointmentId(), app3);

        // Sample Prescription
        Prescription rx1 = new Prescription("RX-5001", app3.getAppointmentId(), pat1.getUserId(), 
                                            pat1.getFullName(), doc3.getUserId(), doc3.getFullName(), 
                                            "Vitamin D3 1000 IU", "1 biji sehari", "Makan selepas sarapan pagi");
        prescriptions.put(rx1.getPrescriptionId(), rx1);

        // Sample Notifications
        notifications.add(new Notification("NOTIF-101", pat1.getUserId(), "Temujanji Disahkan", 
                                          "Temujanji anda dengan Dr. Ahmad Razali telah disahkan untuk hari ini pukul 09:30 AM.", "APPOINTMENT"));
        notifications.add(new Notification("NOTIF-102", pat2.getUserId(), "Kemaskini Giliran", 
                                          "Jururawat Siti Nurhaliza telah mendaftar masuk anda. Sila tunggu di Bilik Menunggu B.", "STATUS_CHANGE"));
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
