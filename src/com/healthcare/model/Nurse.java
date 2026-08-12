package com.healthcare.model;

/**
 * Represents a Nurse user in the healthcare system.
 * Inherits from abstract User class.
 */
public class Nurse extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private String shiftInfo;

    public Nurse(String userId, String username, String password, String fullName, 
                 String email, String phone, String department, String shiftInfo) {
        super(userId, username, password, fullName, email, phone, Role.NURSE);
        this.department = department;
        this.shiftInfo = shiftInfo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getShiftInfo() {
        return shiftInfo;
    }

    public void setShiftInfo(String shiftInfo) {
        this.shiftInfo = shiftInfo;
    }

    @Override
    public String getRoleDescription() {
        return "Nurse (" + department + " Dept, Shift: " + shiftInfo + ")";
    }
}
