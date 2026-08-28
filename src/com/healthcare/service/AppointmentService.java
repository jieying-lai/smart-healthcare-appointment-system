package com.healthcare.service;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.exception.SlotConflictException;
import com.healthcare.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing appointment scheduling, nurse check-ins, doctor consultations, 
 * and slot collision prevention across all patients and doctors.
 */
public class AppointmentService {
    private final DataStorageService dataStorage;
    private final NotificationService notificationService;

    public AppointmentService(DataStorageService dataStorage, NotificationService notificationService) {
        this.dataStorage = dataStorage;
        this.notificationService = notificationService;
    }

    public Appointment bookAppointment(String patientId, String doctorId, LocalDate date, LocalTime time, String reason) 
            throws SlotConflictException, InvalidRecordException {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidRecordException("Reason for visit cannot be empty.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidRecordException("Appointment date cannot be in the past.");
        }

        User patUser = dataStorage.getUsers().get(patientId);
        User docUser = dataStorage.getUsers().get(doctorId);

        if (patUser == null || !(patUser instanceof Patient)) {
            throw new InvalidRecordException("Patient not found.");
        }
        if (docUser == null || !(docUser instanceof Doctor)) {
            throw new InvalidRecordException("Doctor not found.");
        }

        Doctor doctor = (Doctor) docUser;
        Patient patient = (Patient) patUser;

        // Check Slot Collisions
        checkDoctorSlotConflict(doctorId, date, time, null);
        checkPatientSlotConflict(patientId, date, time, null);

        String apptId = "APT-" + (1000 + dataStorage.getAppointments().size() + 1);
        Appointment appt = new Appointment(apptId, patientId, patient.getFullName(), doctorId, 
                                          doctor.getFullName(), date, time, reason.trim(), doctor.getConsultationFee());

        dataStorage.getAppointments().put(apptId, appt);
        dataStorage.saveData();

        notificationService.createNotification(patientId, "Appointment Booked", 
            "Your appointment with Dr. " + doctor.getFullName() + " is confirmed for " + date + " at " + time + ".", "APPOINTMENT");
        notificationService.createNotification(doctorId, "New Appointment Scheduled", 
            "Patient " + patient.getFullName() + " booked an appointment for " + date + " at " + time + ".", "APPOINTMENT");

        return appt;
    }

    public void rescheduleAppointment(String appointmentId, LocalDate newDate, LocalTime newTime) 
            throws SlotConflictException, InvalidRecordException {
        Appointment appt = dataStorage.getAppointments().get(appointmentId);
        if (appt == null) {
            throw new InvalidRecordException("Appointment record not found.");
        }
        if (appt.getStatus() == AppointmentStatus.COMPLETED || 
            appt.getStatus() == AppointmentStatus.CANCELLED || 
            appt.getStatus() == AppointmentStatus.IN_CONSULTATION) {
            throw new InvalidRecordException("Cannot reschedule a completed, in-consultation, or cancelled appointment.");
        }
        if (newDate.isBefore(LocalDate.now())) {
            throw new InvalidRecordException("Rescheduled date cannot be in the past.");
        }

        checkDoctorSlotConflict(appt.getDoctorId(), newDate, newTime, appointmentId);
        checkPatientSlotConflict(appt.getPatientId(), newDate, newTime, appointmentId);

        appt.setAppointmentDate(newDate);
        appt.setAppointmentTime(newTime);
        dataStorage.saveData();

        notificationService.createNotification(appt.getPatientId(), "Appointment Rescheduled", 
            "Your appointment " + appointmentId + " has been rescheduled to " + newDate + " at " + newTime + ".", "APPOINTMENT");
    }

    public void cancelAppointment(String appointmentId, String reason) throws InvalidRecordException {
        Appointment appt = dataStorage.getAppointments().get(appointmentId);
        if (appt == null) {
            throw new InvalidRecordException("Appointment record not found.");
        }
        if (appt.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidRecordException("Cannot cancel an appointment that is already completed.");
        }
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidRecordException("Appointment is already cancelled.");
        }
        if (appt.getStatus() == AppointmentStatus.IN_CONSULTATION) {
            throw new InvalidRecordException("Cannot cancel an appointment that is currently in consultation.");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        dataStorage.saveData();

        notificationService.createNotification(appt.getPatientId(), "Appointment Cancelled", 
            "Appointment " + appointmentId + " has been cancelled. Reason: " + reason, "APPOINTMENT");
        notificationService.createNotification(appt.getDoctorId(), "Appointment Cancelled", 
            "Appointment " + appointmentId + " with " + appt.getPatientName() + " has been cancelled.", "APPOINTMENT");
    }

    public void updateStatus(String appointmentId, AppointmentStatus newStatus) throws InvalidRecordException {
        Appointment appt = dataStorage.getAppointments().get(appointmentId);
        if (appt == null) {
            throw new InvalidRecordException("Appointment record not found.");
        }

        // Validate Nurse Check-In (WAITING status)
        if (newStatus == AppointmentStatus.WAITING) {
            if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
                throw new InvalidRecordException("Only SCHEDULED appointments can be checked in.");
            }
        }

        // Validate Doctor Start Consultation (IN_CONSULTATION status)
        if (newStatus == AppointmentStatus.IN_CONSULTATION) {
            if (appt.getStatus() == AppointmentStatus.SCHEDULED) {
                throw new InvalidRecordException("Patient has not been checked in by the Nurse yet. Appointment status must be WAITING before Doctor can start consultation.");
            }
            if (appt.getStatus() == AppointmentStatus.IN_CONSULTATION) {
                throw new InvalidRecordException("Consultation is already in progress for this patient.");
            }
            if (appt.getStatus() == AppointmentStatus.COMPLETED || appt.getStatus() == AppointmentStatus.CANCELLED) {
                throw new InvalidRecordException("Cannot start consultation for a completed or cancelled appointment.");
            }
        }

        appt.setStatus(newStatus);
        dataStorage.saveData();

        notificationService.createNotification(appt.getPatientId(), "Status Update", 
            "Status for appointment " + appointmentId + " is now: " + newStatus.getLabel(), "STATUS_CHANGE");
    }

    public void recordConsultation(String appointmentId, String notes) throws InvalidRecordException {
        Appointment appt = dataStorage.getAppointments().get(appointmentId);
        if (appt == null) {
            throw new InvalidRecordException("Appointment record not found.");
        }
        if (appt.getStatus() != AppointmentStatus.IN_CONSULTATION) {
            throw new InvalidRecordException("Cannot complete consultation before it has been started. The consultation status must be IN_CONSULTATION.");
        }

        appt.setConsultationNotes(notes);
        appt.setStatus(AppointmentStatus.COMPLETED);
        dataStorage.saveData();

        notificationService.createNotification(appt.getPatientId(), "Consultation Completed", 
            "Dr. " + appt.getDoctorName() + " has completed your consultation for appointment " + appointmentId + ".", "STATUS_CHANGE");
    }

    private void checkDoctorSlotConflict(String doctorId, LocalDate date, LocalTime time, String currentApptId) 
            throws SlotConflictException {
        for (Appointment appt : dataStorage.getAppointments().values()) {
            if (appt.getDoctorId().equals(doctorId) && 
                appt.getAppointmentDate().equals(date) && 
                appt.getAppointmentTime().equals(time) && 
                (currentApptId == null || !appt.getAppointmentId().equals(currentApptId)) &&
                appt.getStatus() != AppointmentStatus.CANCELLED) {
                throw new SlotConflictException("Doctor already has an active appointment scheduled at " + date + " " + time + ".");
            }
        }
    }

    private void checkPatientSlotConflict(String patientId, LocalDate date, LocalTime time, String currentApptId) 
            throws SlotConflictException {
        for (Appointment appt : dataStorage.getAppointments().values()) {
            if (appt.getPatientId().equals(patientId) && 
                appt.getAppointmentDate().equals(date) && 
                appt.getAppointmentTime().equals(time) && 
                (currentApptId == null || !appt.getAppointmentId().equals(currentApptId)) &&
                appt.getStatus() != AppointmentStatus.CANCELLED) {
                throw new SlotConflictException("Patient already has another appointment scheduled at " + date + " " + time + ".");
            }
        }
    }

    public List<Appointment> getAppointmentsForUser(User user) {
        if (user.getRole() == Role.PATIENT) {
            return dataStorage.getAppointments().values().stream()
                .filter(a -> a.getPatientId().equals(user.getUserId()))
                .collect(Collectors.toList());
        } else if (user.getRole() == Role.DOCTOR) {
            return dataStorage.getAppointments().values().stream()
                .filter(a -> a.getDoctorId().equals(user.getUserId()))
                .collect(Collectors.toList());
        }
        return getAllAppointments();
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(dataStorage.getAppointments().values());
    }
}
