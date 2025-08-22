package com.ss.rentmangment;

public class RentPaymentRecord {
    public String paymentId;
    public double amountPaid;
    public String paymentDate;
    public String collectedBy;
    public String notes;

    public RentPaymentRecord() {
        // Default constructor required for calls to DataSnapshot.getValue(RentPaymentRecord.class)
    }

    public RentPaymentRecord(String paymentId, double amountPaid, String paymentDate, String collectedBy, String notes) {
        this.paymentId = paymentId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.collectedBy = collectedBy;
        this.notes = notes;
    }
}
