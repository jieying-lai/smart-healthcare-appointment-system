package com.healthcare.model;

/**
 * Represents a Doctor user in the healthcare system. Inherits from abstract
 * User class.
 */
public class Doctor extends User {
	private static final long serialVersionUID = 1L;

	private String specialization;
	private String department;
	private String roomNumber;
	private double consultationFee;

	public Doctor(String userId, String username, String password, String fullName, String email, String phone,
			String specialization, String department, String roomNumber, double consultationFee) {
		super(userId, username, password, fullName, email, phone, Role.DOCTOR);
		this.specialization = specialization;
		this.department = department;
		this.roomNumber = roomNumber;
		this.consultationFee = consultationFee;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public double getConsultationFee() {
		return consultationFee;
	}

	public void setConsultationFee(double consultationFee) {
		this.consultationFee = consultationFee;
	}

	@Override
	public String getRoleDescription() {
		return "Doctor - " + specialization + " (" + department + " Dept, Room " + roomNumber + ")";
	}
}
