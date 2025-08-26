package com.ss.rentmangment;

public class Expense {
    public String expenseId;
    public String description;
    public double amount;
    public String category;
    public String date;
    public String roomName; // Optional: To associate expense with a room

    public Expense() {
        // Default constructor for Firebase
    }

    public Expense(String expenseId, String description, double amount, String category, String date, String roomName) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.roomName = roomName;
    }
}
