# Smart Healthcare Appointment & Consultation System

An Object-Oriented Healthcare Management System developed in Java with a modern Sky Blue & White Swing GUI, Role-Based Access Control (RBAC), multi-role dashboards, automated notification alerts, data persistence, and analytical reporting.

---

## 🌟 Key Features

1. **User Registration & Profile Management**:
   - Multi-role user creation (Patients, Doctors, Nurses, Pharmacists, Administrators).
   - Profile updating, account deactivation/activation, and password reset.

2. **Transaction & Appointment Management**:
   - Appointment booking, rescheduling, and cancellation with `SlotConflictException` validation (prevents double-booking doctors).

3. **Lifecycle Status Tracking**:
   - Real-time status progression (`Scheduled` → `Waiting` → `In Consultation` → `Completed` / `Cancelled`).
   - High-contrast visual table badges for quick status identification.

4. **Healthcare Operations**:
   - **Nurse Panel**: Patient check-in and active waiting queue management.
   - **Doctor Panel**: Consultation notes, diagnosis recording, and prescription issuance.
   - **Pharmacist Panel**: Prescription order tracking and medication dispensing.
   - **Admin Panel**: Account administration, user management, and executive analytics.

5. **Report Generation & Analytics**:
   - Daily appointment stats, total revenue breakdown, doctor workload metrics, and executive summary text reports.

---

## 🚀 How to Run in Eclipse

1. Open **Eclipse IDE**.
2. Select **File** ➔ **New** ➔ **Java Project**.
3. Uncheck `Use default location` and browse to this repository folder.
4. Click **Finish**.
5. Open `src/com/healthcare/Main.java` ➔ Right-click ➔ **Run As** ➔ **Java Application**.

---

## ⚡ How to Run via Command Line / PowerShell

```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName)
java -cp bin com.healthcare.Main
```

---

## 🔑 Quick Demo Login Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` |
| **Doctor** | `dr_smith` | `doc123` |
| **Nurse** | `nurse_joy` | `nurse123` |
| **Pharmacist** | `pharm_claire` | `pharm123` |
| **Patient** | `john_doe` | `pass123` |
