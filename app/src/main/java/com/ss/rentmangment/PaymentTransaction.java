package com.ss.rentmangment;

public class PaymentTransaction {
    private String transactionId;
    private double amount;
    private String date;
    private String notes;
    private String type;
    private long timestamp;
    private String adminId;

    public PaymentTransaction() {}

    public PaymentTransaction(String transactionId, double amount, String date,
                              String notes, String type, String adminId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.type = type;
        this.adminId = adminId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
}
