package com.ss.rentmangment;

public class RentPaymentRecord {
    private String paymentId;
    private double amountPaid;
    private String paymentDate;
    private String adminId;
    private String notes;
    private String paymentType;
    private double monthlyRent;
    private double remainingAmount;
    private String forMonth;
    private long timestamp;
    private String tenantMobile;

    public RentPaymentRecord() {}

    public RentPaymentRecord(String paymentId, double amountPaid, String paymentDate,
                             String adminId, String notes, String paymentType,
                             double monthlyRent, String forMonth, String tenantMobile) {
        this.paymentId = paymentId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.adminId = adminId;
        this.notes = notes;
        this.paymentType = paymentType;
        this.monthlyRent = monthlyRent;
        this.forMonth = forMonth;
        this.tenantMobile = tenantMobile;
        this.remainingAmount = Math.max(0, monthlyRent - amountPaid);
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(double monthlyRent) { this.monthlyRent = monthlyRent; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getForMonth() { return forMonth; }
    public void setForMonth(String forMonth) { this.forMonth = forMonth; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getTenantMobile() { return tenantMobile; }
    public void setTenantMobile(String tenantMobile) { this.tenantMobile = tenantMobile; }
}
