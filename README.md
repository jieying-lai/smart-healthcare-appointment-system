<div align="center">

  # 🏥 Smart Healthcare Appointment & Consultation System

  **A modular Java GUI desktop application for clinical scheduling, queue orchestration, and consultation management.**

  [![Java](https://img.shields.io/badge/Java-11%20%7C%2017%20%7C%2021-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](#)
  [![GUI](https://img.shields.io/badge/UI-Java%20Swing%20%2F%20AWT-blue?style=flat-square)](#)
  [![Architecture](https://img.shields.io/badge/Pattern-Layered%20MVC%20%2F%20Package%20Decoupled-brightgreen?style=flat-square)](#)

</div>

---

## 📌 Overview

A standalone Java desktop management application engineered for clinical appointment scheduling, patient queue monitoring, and multi-role consultation workflows. 

Built with **Pure Java (Swing/AWT)**, the codebase emphasizes strict Object-Oriented Design (OOD), role-based permissions, automated status lifecycles, and a decoupled package directory structure.

---

## ✨ Key Functionalities

* **Role-Based Access Control (RBAC)**: Polymorphic role permissions separating **Patients**, **Doctors**, and **Administrators**.
* **Consultation Queue & Status Tracking**: Manages appointment states across full operational lifecycles:
  $$\text{Scheduled} \longrightarrow \text{Waiting} \longrightarrow \text{In Consultation} \longrightarrow \text{Completed}$$
  *(with dynamic support for record cancellation and audit integrity)*.
* **Alert & Notification Engine**: Event-driven dialog popups and status banners notifying users of schedule reminders, queue readiness, and slot availability.
* **Administrative Analytics & Reporting**: Summarizes clinic operations, including daily booking counts, consultation turnaround durations, and cancellation rates.

---

## 🚀 How to Run

### Prerequisites

* **Java Development Kit (JDK)**: JDK 11 or higher.

### Compilation & Execution

1. **Clone the repository:**
```bash
git clone [https://github.com/jieying-lai/smart-healthcare-appointment-system.git](https://github.com/jieying-lai/smart-healthcare-appointment-system.git)
cd smart-healthcare-appointment-system

```


2. **Compile the source files:**
```bash
javac -d bin src/com/healthcare/appointment/**/*.java src/com/healthcare/appointment/*.java

```


3. **Launch the application:**
```bash
java -cp bin com.healthcare.appointment.Main

```



---
