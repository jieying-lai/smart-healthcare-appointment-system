package com.healthcare.service;

import com.healthcare.exception.*;
import com.healthcare.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing appointment transactions, queue status transitions, and slot conflict validations.
 */
public class AppointmentService {
    private final DataStorageService dataStorage;
    private final NotificationService notificationService;

    public AppointmentService(DataStorageService dataStorage, NotificationService notificationService) {
        this.dataStorage = dataStorage;
        this.notificationService = notificationService;
    }

    public Appointment bookAppointment(String patientId, String doctorId, LocalDate date, 
                                        LocalTime time, String reason) throws SlotConflictException, InvalidRecordException {
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidRecordException("Cannot book appointment on a past date.");
        }

        User patient = dataStorage.getUsers().get(patientId);
        User doctor = dataStorage.getUsers().get(doctorId);

        if (!(patient instanceof Patient)) {
            throw new InvalidRecordException("Invalid patient record ID.");
        }
        if (!(doctor instanceof Doctor)) {
            throw new InvalidRecordException("Invalid doctor record ID.");
        }

        Doctor doc = (Doctor) doctor;

        // Check slot conflict
        checkSlotConflict(doctorId, date, time, null);

        String apptId = "APT-" + (1000 + dataStorage.getAppointments().size() + 1);
        Appointment appointment = new Appointment(apptId, patient.getUserId(), patient.getFullName(), 
                                                   doc.getUserId(), doc.getFullName(), date, time, 
                                                   reason, doc.getConsultationFee());

        dataStorage.getAppointments().put(apptId, appointment);
        dataStorage.saveData();

        // Send notifications
        notificationService.createNotification(patientId, "Appointment Booked", 
            "Appointment " + apptId + " booked with " + doc.getFullName() + " for " + date + " at " + time + ".", "APPOINTMENT");
        notificationService.createNotification(doctorId, "New Booking", 
            "New appointment " + apptId + " scheduled with " + patient.getFullName() + " for " + date + " at " + time + ".", "APPOINTMENT");

        return appointment;
    }

    public void rescheduleAppointment(String appointmentId, LocalDate newDate, LocalTime newTime) 
            throws SlotConflictException, InvalidRecordException {
        Appointment appt = dataStorage.getAppointments().get(appointmentId);
        if (appt == null) {
            throw new InvalidRecordException("Appointment record not found.");
        }
        if (appt.getStatus() == AppointmentStatus.COMPLETED || appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidRecordException("Cannot reschedule a completed or cancelled appointment.");
        }
        if (newDate.isBefore(LocalDate.now())) {
            throw new InvalidRecordException("Rescheduled date cannot be in the past.");
        }

        checkSlotConflict(appt.getDoctorId(), newDate, newTime, appointmentId);

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
            throw new InvalidRecordException("Completed appointments cannot be cancelled.");
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

        appt.setConsultationNotes(notes);
        appt.setStatus(AppointmentStatus.COMPLETED);
        dataStorage.saveData();

        notificationService.createNotification(appt.getPatientId(), "Consultation Completed", 
            "Dr. " + appt.getDoctorName() + " has completed your consultation for appointment " + appointmentId + ".", "STATUS_CHANGE");
    }

    private void checkSlotConflict(String doctorId, LocalDate date, LocalTime time, String currentApptId) 
            throws SlotConflictException {
        for (Appointment appt : dataStorage.getAppointments().values()) {
            if (appt.getDoctorId().equals(doctorId) && 
                appt.getAppointmentDate().equals(date) && 
                appt.getAppointmentTime().equals(time) && 
                appt.getStatus() != AppointmentStatus.CANCELLED) {
                
                if (currentApptId == null || !appt.getAppointmentId().equals(currentApptId)) {
                    throw new SlotConflictException("Doctor already has an appointment booked on " + 
                                                      date + " at " + time + ". Please select another time slot.");
                }
            }
        }
    }

    public List<Appointment> getAppointmentsForUser(User user) {
        if (user.getRole() == Role.PATIENT) {
            return dataStorage.getAppointments().values().stream()
                .filter(a -> a.getPatientId().equals(user.getUserId()))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
                .collect(Collectors.toList());
        } else if (user.getRole() == Role.DOCTOR) {
            return dataStorage.getAppointments().values().stream()
                .filter(a -> a.getDoctorId().equals(user.getUserId()))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
                .collect(Collectors.toList());
        } else {
            return getAllAppointments();
        }
    }

    public List<Appointment> getAllAppointments() {
        return dataStorage.getAppointments().values().stream()
            .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
            .collect(Collectors.toList());
    }
}
