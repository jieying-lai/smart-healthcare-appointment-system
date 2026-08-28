package com.healthcare.service;

import com.healthcare.model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Data Storage Service utilizing human-readable pipe-delimited plain-text files (.txt).
 * Manages persistent storage for Users, Appointments, Prescriptions, and Notifications.
 */
public class DataStorageService {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String APPOINTMENTS_FILE = DATA_DIR + File.separator + "appointments.txt";
    private static final String PRESCRIPTIONS_FILE = DATA_DIR + File.separator + "prescriptions.txt";
    private static final String NOTIFICATIONS_FILE = DATA_DIR + File.separator + "notifications.txt";

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Appointment> appointments = new LinkedHashMap<>();
    private final Map<String, Prescription> prescriptions = new LinkedHashMap<>();
    private final List<Notification> notifications = new ArrayList<>();

    public DataStorageService() {
        ensureDataDirectoryExists();
        loadAllData();
    }

    private void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public synchronized void loadAllData() {
        users.clear();
        appointments.clear();
        prescriptions.clear();
        notifications.clear();

        loadUsersText();
        loadAppointmentsText();
        loadPrescriptionsText();
        loadNotificationsText();

        if (users.isEmpty()) {
            initSampleData();
            saveData();
        }
    }

    public synchronized void saveData() {
        saveUsersText();
        saveAppointmentsText();
        savePrescriptionsText();
        saveNotificationsText();
    }

    private String cleanStr(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace("\n", " ").replace("\r", " ").trim();
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
                String uId = parts[1];
                String uname = parts[2];
                String pass = parts[3];
                String name = parts[4];
                String email = parts[5];
                String phone = parts[6];

                switch (type) {
                    case "ADMIN":
                        users.put(uId, new Admin(uId, uname, pass, name, email, phone, parts[7]));
                        break;
                    case "DOCTOR":
                        if (parts.length >= 11) {
                            users.put(uId, new Doctor(uId, uname, pass, name, email, phone, parts[7], parts[8], parts[9], Double.parseDouble(parts[10])));
                        }
                        break;
                    case "NURSE":
                        if (parts.length >= 9) {
                            users.put(uId, new Nurse(uId, uname, pass, name, email, phone, parts[7], parts[8]));
                        }
                        break;
                    case "PHARMACIST":
                        if (parts.length >= 9) {
                            users.put(uId, new Pharmacist(uId, uname, pass, name, email, phone, parts[7], parts[8]));
                        }
                        break;
                    case "PATIENT":
                        if (parts.length >= 11) {
                            users.put(uId, new Patient(uId, uname, pass, name, email, phone, parts[7], parts[8], parts[9], parts[10]));
                        }
                        break;
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
                    writer.println("ADMIN|" + cleanStr(a.getUserId()) + "|" + cleanStr(a.getUsername()) + "|" + cleanStr(a.getPassword()) + "|" + 
                                   cleanStr(a.getFullName()) + "|" + cleanStr(a.getEmail()) + "|" + cleanStr(a.getPhone()) + "|" + 
                                   cleanStr(a.getAccessLevel()) + "|" + a.isActive());
                } else if (u instanceof Doctor) {
                    Doctor d = (Doctor) u;
                    writer.println("DOCTOR|" + cleanStr(d.getUserId()) + "|" + cleanStr(d.getUsername()) + "|" + cleanStr(d.getPassword()) + "|" + 
                                   cleanStr(d.getFullName()) + "|" + cleanStr(d.getEmail()) + "|" + cleanStr(d.getPhone()) + "|" + 
                                   cleanStr(d.getSpecialization()) + "|" + cleanStr(d.getDepartment()) + "|" + cleanStr(d.getRoomNumber()) + "|" + 
                                   d.getConsultationFee() + "|" + d.isActive());
                } else if (u instanceof Nurse) {
                    Nurse n = (Nurse) u;
                    writer.println("NURSE|" + cleanStr(n.getUserId()) + "|" + cleanStr(n.getUsername()) + "|" + cleanStr(n.getPassword()) + "|" + 
                                   cleanStr(n.getFullName()) + "|" + cleanStr(n.getEmail()) + "|" + cleanStr(n.getPhone()) + "|" + 
                                   cleanStr(n.getDepartment()) + "|" + cleanStr(n.getShiftInfo()) + "|" + n.isActive());
                } else if (u instanceof Pharmacist) {
                    Pharmacist p = (Pharmacist) u;
                    writer.println("PHARMACIST|" + cleanStr(p.getUserId()) + "|" + cleanStr(p.getUsername()) + "|" + cleanStr(p.getPassword()) + "|" + 
                                   cleanStr(p.getFullName()) + "|" + cleanStr(p.getEmail()) + "|" + cleanStr(p.getPhone()) + "|" + 
                                   cleanStr(p.getLicenseNumber()) + "|" + cleanStr(p.getPharmacySection()) + "|" + p.isActive());
                } else if (u instanceof Patient) {
                    Patient pat = (Patient) u;
                    writer.println("PATIENT|" + cleanStr(pat.getUserId()) + "|" + cleanStr(pat.getUsername()) + "|" + cleanStr(pat.getPassword()) + "|" + 
                                   cleanStr(pat.getFullName()) + "|" + cleanStr(pat.getEmail()) + "|" + cleanStr(pat.getPhone()) + "|" + 
                                   cleanStr(pat.getDateOfBirth()) + "|" + cleanStr(pat.getBloodGroup()) + "|" + cleanStr(pat.getMedicalHistory()) + "|" + 
                                   cleanStr(pat.getEmergencyContact()) + "|" + pat.isActive());
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
                writer.println(cleanStr(a.getAppointmentId()) + "|" + cleanStr(a.getPatientId()) + "|" + cleanStr(a.getPatientName()) + "|" + 
                               cleanStr(a.getDoctorId()) + "|" + cleanStr(a.getDoctorName()) + "|" + a.getAppointmentDate() + "|" + 
                               a.getAppointmentTime() + "|" + a.getStatus().name() + "|" + cleanStr(a.getReasonForVisit()) + "|" + 
                               cleanStr(a.getConsultationNotes()) + "|" + a.getFee());
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
                    if (parts.length >= 12) {
                        rx.setDispensedByPharmacistName(parts[10]);
                        rx.setPharmacySection(parts[11]);
                    }
                    prescriptions.put(rx.getPrescriptionId(), rx);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading prescriptions text file: " + e.getMessage());
        }
    }

    private void savePrescriptionsText() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PRESCRIPTIONS_FILE))) {
            writer.println("# Prescription Data Storage: rxId|apptId|patientId|patientName|doctorId|doctorName|medication|dosage|instructions|status|dispensedByPharmacistName|pharmacySection");
            for (Prescription rx : prescriptions.values()) {
                writer.println(cleanStr(rx.getPrescriptionId()) + "|" + cleanStr(rx.getAppointmentId()) + "|" + cleanStr(rx.getPatientId()) + "|" + 
                               cleanStr(rx.getPatientName()) + "|" + cleanStr(rx.getDoctorId()) + "|" + cleanStr(rx.getDoctorName()) + "|" + 
                               cleanStr(rx.getMedicationName()) + "|" + cleanStr(rx.getDosage()) + "|" + cleanStr(rx.getInstructions()) + "|" + 
                               rx.getStatus().name() + "|" + cleanStr(rx.getDispensedByPharmacistName()) + "|" + cleanStr(rx.getPharmacySection()));
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
        System.out.println("Initializing expanded Malaysian healthcare accounts (5 Doctors, 3 Pharmacists, 8 Nurses, 10 Patients)...");

        // 1. Admin
        Admin admin = new Admin("USR-001", "admin", "admin123", "Lai Jie Ying", 
                                "jieying@pantai.com.my", "+60 12-345 6789", "Level 5 System Administrator");
        users.put(admin.getUserId(), admin);

        // 2. 5 Doctors
        Doctor doc1 = new Doctor("USR-002", "dr_razali", "doc123", "Dr. Ahmad Razali bin Hassan", 
                                 "razali@pantai.com.my", "+60 19-223 4567", "Kardiologi (Cardiology)", 
                                 "Pusat Jantung Negara", "Bilik 302", 150.00);
        Doctor doc2 = new Doctor("USR-003", "dr_lee", "doc123", "Dr. Lee Mei Ling", 
                                 "meiling@pantai.com.my", "+60 16-334 5678", "Dermatologi (Skin Care)", 
                                 "Jabatan Kulit", "Bilik 105", 120.00);
        Doctor doc3 = new Doctor("USR-004", "dr_kavitha", "doc123", "Dr. Kavitha a/p Subramaniam", 
                                 "kavitha@pantai.com.my", "+60 17-445 6789", "Pediatrik (Child Health)", 
                                 "Jabatan Kanak-Kanak", "Bilik 210", 110.00);
        Doctor doc4 = new Doctor("USR-009", "dr_suresh", "doc123", "Dr. Suresh Kumar a/l Raman", 
                                 "suresh@pantai.com.my", "+60 11-556 7890", "Ortopedik (Orthopedics)", 
                                 "Jabatan Tulang", "Bilik 401", 160.00);
        Doctor doc5 = new Doctor("USR-010", "dr_nurul", "doc123", "Dr. Nurul Huda binti Azman", 
                                 "nurul.huda@pantai.com.my", "+60 13-667 8901", "Neurologi (Neurology)", 
                                 "Pusat Neuro", "Bilik 505", 180.00);
        users.put(doc1.getUserId(), doc1);
        users.put(doc2.getUserId(), doc2);
        users.put(doc3.getUserId(), doc3);
        users.put(doc4.getUserId(), doc4);
        users.put(doc5.getUserId(), doc5);

        // 3. 3 Pharmacists
        Pharmacist pharm1 = new Pharmacist("USR-006", "pharm_wong", "pharm123", "Wong Wei Jun (Pharmacist)", 
                                           "weijun@pantai.com.my", "+60 14-667 8901", "MPS-994821", "Main Pharmacy Store");
        Pharmacist pharm2 = new Pharmacist("USR-011", "pharm_priya", "pharm123", "Priya a/p Ramesh (Pharmacist)", 
                                           "priya@pantai.com.my", "+60 17-778 9012", "OPS-883712", "Outpatient Pharmacy Counter 2");
        Pharmacist pharm3 = new Pharmacist("USR-012", "pharm_hafiz", "pharm123", "Muhammad Hafiz bin Rosli (Pharmacist)", 
                                           "hafiz@pantai.com.my", "+60 18-889 0123", "EPS-774631", "Emergency Pharmacy Store");
        users.put(pharm1.getUserId(), pharm1);
        users.put(pharm2.getUserId(), pharm2);
        users.put(pharm3.getUserId(), pharm3);

        // 4. 8 Nurses
        Nurse nurse1 = new Nurse("USR-005", "nurse_siti", "nurse123", "Nurse Siti Nurhaliza binti Ali", 
                                 "snurhaliza@pantai.com.my", "+60 13-556 7890", "Outpatient Department", "Morning Shift (08:00 - 16:00)");
        Nurse nurse2 = new Nurse("USR-013", "nurse_sockling", "nurse123", "Nurse Tan Sock Ling", 
                                 "sockling@pantai.com.my", "+60 12-223 3445", "Cardiology Clinic", "Morning Shift (08:00 - 16:00)");
        Nurse nurse3 = new Nurse("USR-014", "nurse_anusha", "nurse123", "Nurse Anusha a/p Mohan", 
                                 "anusha@pantai.com.my", "+60 16-334 4556", "Pediatric Ward", "Afternoon Shift (14:00 - 22:00)");
        Nurse nurse4 = new Nurse("USR-015", "nurse_faridah", "nurse123", "Nurse Faridah binti Abdullah", 
                                 "faridah@pantai.com.my", "+60 19-445 5667", "Triage Reception Desk", "Morning Shift (08:00 - 16:00)");
        Nurse nurse5 = new Nurse("USR-016", "nurse_michelle", "nurse123", "Nurse Michelle Lim Siew Yee", 
                                 "michelle@pantai.com.my", "+60 17-556 6778", "Emergency Room Triage", "Night Shift (22:00 - 08:00)");
        Nurse nurse6 = new Nurse("USR-017", "nurse_kavitha", "nurse123", "Nurse Kavitha a/p Balan", 
                                 "kavithab@pantai.com.my", "+60 14-667 7889", "Dermatology Clinic", "Afternoon Shift (14:00 - 22:00)");
        Nurse nurse7 = new Nurse("USR-018", "nurse_zainab", "nurse123", "Nurse Zainab binti Kassim", 
                                 "zainab@pantai.com.my", "+60 11-778 8990", "Orthopedic Outpatient", "Morning Shift (08:00 - 16:00)");
        Nurse nurse8 = new Nurse("USR-019", "nurse_cheah", "nurse123", "Nurse Cheah Poh Ling", 
                                 "cheah@pantai.com.my", "+60 18-990 0112", "General Waiting Hall", "Day Shift (09:00 - 17:00)");
        users.put(nurse1.getUserId(), nurse1);
        users.put(nurse2.getUserId(), nurse2);
        users.put(nurse3.getUserId(), nurse3);
        users.put(nurse4.getUserId(), nurse4);
        users.put(nurse5.getUserId(), nurse5);
        users.put(nurse6.getUserId(), nurse6);
        users.put(nurse7.getUserId(), nurse7);
        users.put(nurse8.getUserId(), nurse8);

        // 5. 10 Patients
        Patient pat1 = new Patient("USR-007", "tan_ah_hock", "pass123", "Tan Ah Hock", 
                                   "tahock@gmail.com", "+60 12-778 9012", "1988-05-14", "O+", "High Blood Pressure, Penicillin Allergy", "+60 12-999 8877");
        Patient pat2 = new Patient("USR-008", "nur_aisyah", "pass123", "Nur Aisyah binti Ismail", 
                                   "aisyah@yahoo.com.my", "+60 18-889 0123", "1995-11-20", "A-", "No chronic medical history", "+60 11-2233 4455");
        Patient pat3 = new Patient("USR-020", "chong_wei_ming", "pass123", "Chong Wei Ming", 
                                   "weiming@gmail.com", "+60 12-111 2233", "1990-03-25", "B+", "Mild Asthma", "+60 12-333 4455");
        Patient pat4 = new Patient("USR-021", "kamala_krishnan", "pass123", "Kamala a/p Krishnan", 
                                   "kamala@yahoo.com", "+60 16-222 3344", "1982-08-12", "AB+", "Type 2 Diabetes", "+60 16-444 5566");
        Patient pat5 = new Patient("USR-022", "ahmad_faiz", "pass123", "Ahmad Faiz bin Zulkifli", 
                                   "faiz@hotmail.com", "+60 19-333 4455", "1998-01-30", "O-", "No prior medical history", "+60 19-555 6677");
        Patient pat6 = new Patient("USR-023", "lim_bee_hoon", "pass123", "Lim Bee Hoon", 
                                   "beehoon@gmail.com", "+60 17-444 5566", "1975-12-05", "A+", "High Cholesterol", "+60 17-666 7788");
        Patient pat7 = new Patient("USR-024", "suresh_devan", "pass123", "Suresh a/l Devan", 
                                   "suresh.dev@gmail.com", "+60 11-555 6677", "1986-07-18", "B-", "Knee Joint Stiffness", "+60 11-777 8899");
        Patient pat8 = new Patient("USR-025", "noraini_salleh", "pass123", "Noraini binti Mohd Salleh", 
                                   "noraini@yahoo.com.my", "+60 13-666 7788", "1992-09-09", "O+", "Migraine History", "+60 13-888 9900");
        Patient pat9 = new Patient("USR-026", "kevin_ng", "pass123", "Kevin Ng Chee Keong", 
                                   "kevin.ng@gmail.com", "+60 14-777 8899", "1994-04-02", "AB-", "Penicillin & Dust Allergy", "+60 14-999 0011");
        Patient pat10 = new Patient("USR-027", "siti_fatimah", "pass123", "Siti Fatimah binti Hamzah", 
                                    "fatimah@gmail.com", "+60 18-888 9900", "2001-06-22", "A+", "No chronic history", "+60 18-000 1122");

        users.put(pat1.getUserId(), pat1);
        users.put(pat2.getUserId(), pat2);
        users.put(pat3.getUserId(), pat3);
        users.put(pat4.getUserId(), pat4);
        users.put(pat5.getUserId(), pat5);
        users.put(pat6.getUserId(), pat6);
        users.put(pat7.getUserId(), pat7);
        users.put(pat8.getUserId(), pat8);
        users.put(pat9.getUserId(), pat9);
        users.put(pat10.getUserId(), pat10);

        // Sample Appointments (12 realistic records across all 10 patients and 5 doctors)
        LocalDate today = LocalDate.now();
        Appointment app1 = new Appointment("APT-1001", pat1.getUserId(), pat1.getFullName(), 
                                            doc1.getUserId(), doc1.getFullName(), today, 
                                            LocalTime.of(9, 30), "Annual Cardiology Checkup", doc1.getConsultationFee());
        app1.setStatus(AppointmentStatus.SCHEDULED);

        Appointment app2 = new Appointment("APT-1002", pat2.getUserId(), pat2.getFullName(), 
                                            doc2.getUserId(), doc2.getFullName(), today, 
                                            LocalTime.of(10, 30), "Skin Consultation & Rash Check", doc2.getConsultationFee());
        app2.setStatus(AppointmentStatus.WAITING);

        Appointment app3 = new Appointment("APT-1003", pat3.getUserId(), pat3.getFullName(), 
                                            doc3.getUserId(), doc3.getFullName(), today.minusDays(1), 
                                            LocalTime.of(14, 0), "Routine Pediatric Checkup", doc3.getConsultationFee());
        app3.setStatus(AppointmentStatus.COMPLETED);
        app3.setConsultationNotes("Patient in healthy condition. Prescribed Vitamin D3 1000 IU.");

        Appointment app4 = new Appointment("APT-1004", pat4.getUserId(), pat4.getFullName(), 
                                            doc4.getUserId(), doc4.getFullName(), today, 
                                            LocalTime.of(11, 0), "Knee Joint Pain Consultation", doc4.getConsultationFee());
        app4.setStatus(AppointmentStatus.WAITING);

        Appointment app5 = new Appointment("APT-1005", pat5.getUserId(), pat5.getFullName(), 
                                            doc5.getUserId(), doc5.getFullName(), today.plusDays(1), 
                                            LocalTime.of(15, 0), "Neurology Headache Evaluation", doc5.getConsultationFee());
        app5.setStatus(AppointmentStatus.SCHEDULED);

        Appointment app6 = new Appointment("APT-1006", pat6.getUserId(), pat6.getFullName(), 
                                            doc1.getUserId(), doc1.getFullName(), today.minusDays(2), 
                                            LocalTime.of(9, 0), "Cardiovascular Hypertension Check", doc1.getConsultationFee());
        app6.setStatus(AppointmentStatus.COMPLETED);
        app6.setConsultationNotes("ECG normal. Continue Low-Sodium diet and Amlodipine 5mg.");

        Appointment app7 = new Appointment("APT-1007", pat7.getUserId(), pat7.getFullName(), 
                                            doc2.getUserId(), doc2.getFullName(), today.minusDays(3), 
                                            LocalTime.of(11, 30), "Allergies & Eczema Consultation", doc2.getConsultationFee());
        app7.setStatus(AppointmentStatus.COMPLETED);
        app7.setConsultationNotes("Eczema flare-up on forearm. Prescribed Hydrocortisone cream 1%.");

        Appointment app8 = new Appointment("APT-1008", pat8.getUserId(), pat8.getFullName(), 
                                            doc3.getUserId(), doc3.getFullName(), today.minusDays(4), 
                                            LocalTime.of(10, 0), "Flu & Fever Examination", doc3.getConsultationFee());
        app8.setStatus(AppointmentStatus.COMPLETED);
        app8.setConsultationNotes("Mild viral infection. Rest well and take Paracetamol 500mg after meals.");

        Appointment app9 = new Appointment("APT-1009", pat9.getUserId(), pat9.getFullName(), 
                                            doc4.getUserId(), doc4.getFullName(), today.minusDays(5), 
                                            LocalTime.of(16, 0), "Sports Wrist Strain Consultation", doc4.getConsultationFee());
        app9.setStatus(AppointmentStatus.COMPLETED);
        app9.setConsultationNotes("Sprained right wrist during basketball. Wrist brace applied.");

        Appointment app10 = new Appointment("APT-1010", pat10.getUserId(), pat10.getFullName(), 
                                             doc5.getUserId(), doc5.getFullName(), today.minusDays(6), 
                                             LocalTime.of(15, 30), "Migraine & Tension Headache Review", doc5.getConsultationFee());
        app10.setStatus(AppointmentStatus.COMPLETED);
        app10.setConsultationNotes("Tension headache caused by prolonged screen time. Sleep hygiene advice provided.");

        Appointment app11 = new Appointment("APT-1011", pat1.getUserId(), pat1.getFullName(), 
                                             doc2.getUserId(), doc2.getFullName(), today.minusDays(7), 
                                             LocalTime.of(14, 30), "Dry Skin & Itching Follow-up", doc2.getConsultationFee());
        app11.setStatus(AppointmentStatus.COMPLETED);
        app11.setConsultationNotes("Dry skin condition improving. Prescribed Cetirizine 10mg for nighttime allergy.");

        Appointment app12 = new Appointment("APT-1012", pat2.getUserId(), pat2.getFullName(), 
                                             doc1.getUserId(), doc1.getFullName(), today.minusDays(8), 
                                             LocalTime.of(10, 30), "Blood Pressure Screening", doc1.getConsultationFee());
        app12.setStatus(AppointmentStatus.COMPLETED);
        app12.setConsultationNotes("Blood pressure normal at 118/76 mmHg. Maintain regular exercise.");

        appointments.put(app1.getAppointmentId(), app1);
        appointments.put(app2.getAppointmentId(), app2);
        appointments.put(app3.getAppointmentId(), app3);
        appointments.put(app4.getAppointmentId(), app4);
        appointments.put(app5.getAppointmentId(), app5);
        appointments.put(app6.getAppointmentId(), app6);
        appointments.put(app7.getAppointmentId(), app7);
        appointments.put(app8.getAppointmentId(), app8);
        appointments.put(app9.getAppointmentId(), app9);
        appointments.put(app10.getAppointmentId(), app10);
        appointments.put(app11.getAppointmentId(), app11);
        appointments.put(app12.getAppointmentId(), app12);

        // Sample Prescriptions (with dispensing Pharmacist & Pharmacy Section tracking)
        Prescription rx1 = new Prescription("RX-5001", app3.getAppointmentId(), pat3.getUserId(), 
                                            pat3.getFullName(), doc3.getUserId(), doc3.getFullName(), 
                                            "Vitamin D3 1000 IU", "1 tablet daily", "Take 1 tablet daily after breakfast with water.");
        rx1.setStatus(PrescriptionStatus.DISPENSED);
        rx1.setDispensedByPharmacistId(pharm1.getUserId());
        rx1.setDispensedByPharmacistName(pharm1.getFullName());
        rx1.setPharmacySection(pharm1.getPharmacySection());

        Prescription rx2 = new Prescription("RX-5002", app4.getAppointmentId(), pat4.getUserId(), 
                                            pat4.getFullName(), doc4.getUserId(), doc4.getFullName(), 
                                            "Glucosamine Sulfate 500mg", "2 capsules daily", "Take after lunch for joint lubrication.");
        rx2.setStatus(PrescriptionStatus.PREPARING);
        rx2.setDispensedByPharmacistId(pharm2.getUserId());
        rx2.setDispensedByPharmacistName(pharm2.getFullName());
        rx2.setPharmacySection(pharm2.getPharmacySection());

        Prescription rx3 = new Prescription("RX-5003", app6.getAppointmentId(), pat6.getUserId(), 
                                            pat6.getFullName(), doc1.getUserId(), doc1.getFullName(), 
                                            "Amlodipine 5mg", "1 tablet daily", "Take 1 tablet in the morning for blood pressure control.");
        rx3.setStatus(PrescriptionStatus.DISPENSED);
        rx3.setDispensedByPharmacistId(pharm1.getUserId());
        rx3.setDispensedByPharmacistName(pharm1.getFullName());
        rx3.setPharmacySection(pharm1.getPharmacySection());

        Prescription rx4 = new Prescription("RX-5004", app7.getAppointmentId(), pat7.getUserId(), 
                                            pat7.getFullName(), doc2.getUserId(), doc2.getFullName(), 
                                            "Hydrocortisone Cream 1%", "Apply twice daily", "Apply thin layer to affected skin area.");
        rx4.setStatus(PrescriptionStatus.DISPENSED);
        rx4.setDispensedByPharmacistId(pharm2.getUserId());
        rx4.setDispensedByPharmacistName(pharm2.getFullName());
        rx4.setPharmacySection(pharm2.getPharmacySection());

        Prescription rx5 = new Prescription("RX-5005", app8.getAppointmentId(), pat8.getUserId(), 
                                            pat8.getFullName(), doc3.getUserId(), doc3.getFullName(), 
                                            "Paracetamol 500mg", "1-2 tablets every 6 hours", "Take after meals for fever relief.");
        rx5.setStatus(PrescriptionStatus.DISPENSED);
        rx5.setDispensedByPharmacistId(pharm3.getUserId());
        rx5.setDispensedByPharmacistName(pharm3.getFullName());
        rx5.setPharmacySection(pharm3.getPharmacySection());

        Prescription rx6 = new Prescription("RX-5006", app11.getAppointmentId(), pat1.getUserId(), 
                                             pat1.getFullName(), doc2.getUserId(), doc2.getFullName(), 
                                             "Cetirizine 10mg", "1 tablet at bedtime", "Take 1 tablet at night for itching relief.");
        rx6.setStatus(PrescriptionStatus.DISPENSED);
        rx6.setDispensedByPharmacistId(pharm1.getUserId());
        rx6.setDispensedByPharmacistName(pharm1.getFullName());
        rx6.setPharmacySection(pharm1.getPharmacySection());

        prescriptions.put(rx1.getPrescriptionId(), rx1);
        prescriptions.put(rx2.getPrescriptionId(), rx2);
        prescriptions.put(rx3.getPrescriptionId(), rx3);
        prescriptions.put(rx4.getPrescriptionId(), rx4);
        prescriptions.put(rx5.getPrescriptionId(), rx5);
        prescriptions.put(rx6.getPrescriptionId(), rx6);

        // Sample Notifications
        notifications.add(new Notification("NOTIF-101", pat1.getUserId(), "Appointment Confirmed", 
                                          "Your appointment with Dr. Ahmad Razali has been confirmed for today at 09:30 AM.", "APPOINTMENT"));
        notifications.add(new Notification("NOTIF-102", pat2.getUserId(), "Queue Status Update", 
                                          "Nurse Siti Nurhaliza has checked you in. Please proceed to Waiting Room B.", "STATUS_CHANGE"));
        notifications.add(new Notification("NOTIF-103", pat3.getUserId(), "Medication Dispensed", 
                                          "Your medication (Vitamin D3 1000 IU) has been dispensed by Pharmacist Wong Wei Jun at Main Pharmacy Store.", "MEDICATION"));
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
