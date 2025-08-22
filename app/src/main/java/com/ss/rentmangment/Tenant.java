package com.ss.rentmangment;

public class Tenant {
    public String tenantId;
    public String name;
    public String mobile;
    public String email;
    public String roomNumber;
    public String assignedRoomKey;
    public String assignedRoomName;
    public String leaseStartDate;
    public String leaseEndDate;
    public double rentAmount;
    public double securityDeposit;
    public String status;
    public String emergencyContactName;
    public String emergencyContactPhone;
    public String idProofType;
    public String idProofNumber;
    public String notes;
    public String joiningDate;

    public String tenantType; // "Family" or "Students"
    public String checkInDate;   // Expected format: "dd/MM/yyyy"
    public String checkOutDate;  // Expected format: "dd/MM/yyyy" (optional, can be null or empty if still active)
    // Default constructor for Firebase
    public Tenant() {}

    // Complete constructor
    public Tenant(String tenantId, String name, String mobile, String email,
                  String roomNumber, String assignedRoomKey, String assignedRoomName,
                  String leaseStartDate, String leaseEndDate, double rentAmount,
                  double securityDeposit, String status, String emergencyContactName,
                  String emergencyContactPhone, String idProofType, String idProofNumber,
                  String notes, String tenantType) {
        this.tenantId = tenantId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.roomNumber = roomNumber;
        this.assignedRoomKey = assignedRoomKey;
        this.assignedRoomName = assignedRoomName;
        this.leaseStartDate = leaseStartDate;
        this.leaseEndDate = leaseEndDate;
        this.rentAmount = rentAmount;
        this.securityDeposit = securityDeposit;
        this.status = status;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.notes = notes;
        this.tenantType = tenantType;
    }

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAssignedRoomKey() { return assignedRoomKey; }
    public void setAssignedRoomKey(String assignedRoomKey) { this.assignedRoomKey = assignedRoomKey; }

    public String getAssignedRoomName() { return assignedRoomName; }
    public void setAssignedRoomName(String assignedRoomName) { this.assignedRoomName = assignedRoomName; }

    public String getLeaseStartDate() { return leaseStartDate; }
    public void setLeaseStartDate(String leaseStartDate) { this.leaseStartDate = leaseStartDate; }

    public String getLeaseEndDate() { return leaseEndDate; }
    public void setLeaseEndDate(String leaseEndDate) { this.leaseEndDate = leaseEndDate; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }

    public double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }

    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTenantType() { return tenantType; }
    public void setTenantType(String tenantType) { this.tenantType = tenantType; }
}
