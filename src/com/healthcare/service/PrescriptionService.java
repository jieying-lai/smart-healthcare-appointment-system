package com.healthcare.service;

import com.healthcare.exception.InvalidRecordException;
import com.healthcare.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing medication prescription workflow from issue to dispensing.
 */
public class PrescriptionService {
    private final DataStorageService dataStorage;
    private final NotificationService notificationService;

    public PrescriptionService(DataStorageService dataStorage, NotificationService notificationService) {
        this.dataStorage = dataStorage;
        this.notificationService = notificationService;
    }

    public Prescription createPrescription(String appointmentId, String patientId, String patientName, 
                                            String doctorId, String doctorName, String medicationName, 
                                            String dosage, String instructions) throws InvalidRecordException {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            throw new InvalidRecordException("Medication name cannot be empty.");
        }

        String rxId = "RX-" + (5000 + dataStorage.getPrescriptions().size() + 1);
        Prescription rx = new Prescription(rxId, appointmentId, patientId, patientName, doctorId, 
                                          doctorName, medicationName, dosage, instructions);

        dataStorage.getPrescriptions().put(rxId, rx);
        dataStorage.saveData();

        notificationService.createNotification(patientId, "Prescription Issued", 
            "New prescription (" + medicationName + ") issued by Dr. " + doctorName + ".", "MEDICATION");

        return rx;
    }

    public void updatePrescriptionStatus(String rxId, PrescriptionStatus status) throws InvalidRecordException {
        Prescription rx = dataStorage.getPrescriptions().get(rxId);
        if (rx == null) {
            throw new InvalidRecordException("Prescription record not found.");
        }

        rx.setStatus(status);
        dataStorage.saveData();

        if (status == PrescriptionStatus.DISPENSED) {
            notificationService.createNotification(rx.getPatientId(), "Medication Ready", 
                "Your medication (" + rx.getMedicationName() + ") has been dispensed by the pharmacy.", "MEDICATION");
        }
    }

    public List<Prescription> getPrescriptionsForPatient(String patientId) {
        return dataStorage.getPrescriptions().values().stream()
            .filter(rx -> rx.getPatientId().equals(patientId))
            .collect(Collectors.toList());
    }

    public List<Prescription> getAllPrescriptions() {
        return new ArrayList<>(dataStorage.getPrescriptions().values());
    }
}
