package com.ss.rentmangment;

public class RentPayment {
    public String paymentId;
    public String tenantId;
    public String roomNumber;
    public double amountPaid;
    public String paymentDate;
    public String paymentMode;
    public String notes;

    public RentPayment() { }

    public RentPayment(String paymentId, String tenantId, String roomNumber,
                       double amountPaid, String paymentDate, String paymentMode, String notes) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.roomNumber = roomNumber;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMode = paymentMode;
        this.notes = notes;
    }
}
