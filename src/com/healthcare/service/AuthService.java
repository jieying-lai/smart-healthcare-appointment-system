package com.healthcare.service;

import com.healthcare.exception.*;
import com.healthcare.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service managing Authentication, Registration, and User Profile operations.
 */
public class AuthService {
    private final DataStorageService dataStorage;
    private User currentUser;

    public AuthService(DataStorageService dataStorage) {
        this.dataStorage = dataStorage;
    }

    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Username and password fields cannot be empty.");
        }

        for (User user : dataStorage.getUsers().values()) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                if (!user.isActive()) {
                    throw new AuthenticationException("Account deactivated. Contact system administrator.");
                }
                if (user.getPassword().equals(password)) {
                    this.currentUser = user;
                    return user;
                } else {
                    throw new AuthenticationException("Invalid password. Please check your credentials.");
                }
            }
        }
        throw new AuthenticationException("Username not found in system.");
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void registerPatient(String username, String password, String fullName, String email, 
                                String phone, String dateOfBirth, String bloodGroup, 
                                String medicalHistory, String emergencyContact) throws InvalidRecordException {
        validateUsernameUnique(username);
        
        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Patient patient = new Patient(userId, username, password, fullName, email, phone, 
                                      dateOfBirth, bloodGroup, medicalHistory, emergencyContact);
        
        dataStorage.getUsers().put(userId, patient);
        dataStorage.saveData();
    }

    public void createUser(User newUser) throws InvalidRecordException {
        validateUsernameUnique(newUser.getUsername());
        dataStorage.getUsers().put(newUser.getUserId(), newUser);
        dataStorage.saveData();
    }

    public void updateUserProfile(User updatedUser) {
        dataStorage.getUsers().put(updatedUser.getUserId(), updatedUser);
        if (currentUser != null && currentUser.getUserId().equals(updatedUser.getUserId())) {
            this.currentUser = updatedUser;
        }
        dataStorage.saveData();
    }

    public void resetPassword(String userId, String newPassword) throws InvalidRecordException {
        User user = dataStorage.getUsers().get(userId);
        if (user == null) {
            throw new InvalidRecordException("User record not found.");
        }
        if (newPassword == null || newPassword.trim().length() < 4) {
            throw new InvalidRecordException("New password must be at least 4 characters.");
        }
        user.setPassword(newPassword);
        dataStorage.saveData();
    }

    public void toggleUserActiveStatus(String userId, boolean active) throws InvalidRecordException {
        User user = dataStorage.getUsers().get(userId);
        if (user == null) {
            throw new InvalidRecordException("User record not found.");
        }
        user.setActive(active);
        dataStorage.saveData();
    }

    private void validateUsernameUnique(String username) throws InvalidRecordException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidRecordException("Username cannot be blank.");
        }
        for (User u : dataStorage.getUsers().values()) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                throw new InvalidRecordException("Username '" + username + "' is already taken.");
            }
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(dataStorage.getUsers().values());
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (User user : dataStorage.getUsers().values()) {
            if (user instanceof Doctor && user.isActive()) {
                doctors.add((Doctor) user);
            }
        }
        return doctors;
    }
}
