package com.healthcare.service;

import com.healthcare.exception.*;
import com.healthcare.model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service managing Authentication, Registration, and User Profile operations
 * with strict input validation.
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

	public void registerPatient(String username, String password, String fullName, String email, String phone,
			String dateOfBirth, String bloodGroup, String medicalHistory, String emergencyContact)
			throws InvalidRecordException {
		validateUsername(username);
		validateUsernameUnique(username);
		validatePassword(password);
		validateFullName(fullName);
		validateEmail(email);
		validatePhone(phone);
		validateDate(dateOfBirth, "Date of birth");

		String userId = "USR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		Patient patient = new Patient(userId, username.trim(), password.trim(), fullName.trim(), email.trim(),
				phone.trim(), dateOfBirth.trim(), bloodGroup, medicalHistory != null ? medicalHistory.trim() : "",
				emergencyContact != null ? emergencyContact.trim() : "");

		dataStorage.getUsers().put(userId, patient);
		dataStorage.saveData();
	}

	public void createUser(User newUser) throws InvalidRecordException {
		validateUsername(newUser.getUsername());
		validateUsernameUnique(newUser.getUsername());
		validatePassword(newUser.getPassword());
		validateFullName(newUser.getFullName());
		validateEmail(newUser.getEmail());
		validatePhone(newUser.getPhone());

		dataStorage.getUsers().put(newUser.getUserId(), newUser);
		dataStorage.saveData();
	}

	public void updateUserProfile(User updatedUser) throws InvalidRecordException {
		validateFullName(updatedUser.getFullName());
		validateEmail(updatedUser.getEmail());
		validatePhone(updatedUser.getPhone());
		validatePassword(updatedUser.getPassword());

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
		validatePassword(newPassword);
		user.setPassword(newPassword.trim());
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

	// Input Validation Routines
	public void validateUsername(String username) throws InvalidRecordException {
		if (username == null || username.trim().isEmpty()) {
			throw new InvalidRecordException("Username cannot be blank.");
		}
		if (username.trim().length() < 3) {
			throw new InvalidRecordException("Username must be at least 3 characters long.");
		}
		if (!username.trim().matches("^[a-zA-Z0-9_]+$")) {
			throw new InvalidRecordException(
					"Username contains invalid/garbled characters. Use letters, numbers, or underscores only.");
		}
	}

	private void validateUsernameUnique(String username) throws InvalidRecordException {
		for (User u : dataStorage.getUsers().values()) {
			if (u.getUsername().equalsIgnoreCase(username.trim())) {
				throw new InvalidRecordException("Username '" + username + "' is already taken.");
			}
		}
	}

	public void validatePassword(String password) throws InvalidRecordException {
		if (password == null || password.trim().isEmpty()) {
			throw new InvalidRecordException("Password cannot be blank.");
		}
		if (password.trim().length() < 6) {
			throw new InvalidRecordException("Password must be at least 6 characters long.");
		}
	}

	public void validateFullName(String fullName) throws InvalidRecordException {
		if (fullName == null || fullName.trim().isEmpty()) {
			throw new InvalidRecordException("Full name cannot be blank.");
		}
		if (fullName.trim().length() < 2) {
			throw new InvalidRecordException("Full name must be at least 2 characters long.");
		}
		if (fullName.trim().matches(".*[<>{}\\\\/\\\\\\\\]+.*")) {
			throw new InvalidRecordException("Full name contains invalid symbols or garbled characters.");
		}
	}

	public void validateEmail(String email) throws InvalidRecordException {
		if (email == null || email.trim().isEmpty()) {
			throw new InvalidRecordException("Email address cannot be blank.");
		}
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.trim().matches(emailRegex)) {
			throw new InvalidRecordException("Invalid email format (e.g. user@example.com).");
		}
	}

	public void validatePhone(String phone) throws InvalidRecordException {
		if (phone == null || phone.trim().isEmpty()) {
			throw new InvalidRecordException("Phone number cannot be blank.");
		}
		if (!phone.trim().matches("^[+\\d\\s-]{8,20}$")) {
			throw new InvalidRecordException("Invalid phone number format (e.g. +60123456789).");
		}
	}

	public void validateDate(String dateStr, String fieldName) throws InvalidRecordException {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			throw new InvalidRecordException(fieldName + " cannot be blank.");
		}
		try {
			LocalDate.parse(dateStr.trim());
		} catch (Exception e) {
			throw new InvalidRecordException("Invalid " + fieldName + " format. Use YYYY-MM-DD (e.g. 1995-08-15).");
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
