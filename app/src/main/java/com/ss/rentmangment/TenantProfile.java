package com.ss.rentmangment;

import java.util.List;

public class TenantProfile {
    private Tenant tenant;
    private boolean paidForCurrentMonth;
    private String paymentStatus;
    private double amountPaid;
    private double remainingAmount;
    private List<PaymentTransaction> transactionHistory;
    private String tenantStatus; // Active or Left

    public TenantProfile(Tenant tenant) {
        this.tenant = tenant;
        this.paidForCurrentMonth = false;
        this.paymentStatus = "PENDING";
        this.amountPaid = 0;
        this.remainingAmount = tenant.rentAmount;
        this.tenantStatus = tenant.status;
    }

    // All existing getters and setters...
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public boolean isPaidForCurrentMonth() { return paidForCurrentMonth; }
    public void setPaidForCurrentMonth(boolean paidForCurrentMonth) { this.paidForCurrentMonth = paidForCurrentMonth; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
        this.remainingAmount = tenant.rentAmount - amountPaid;
    }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public List<PaymentTransaction> getTransactionHistory() { return transactionHistory; }
    public void setTransactionHistory(List<PaymentTransaction> transactionHistory) { this.transactionHistory = transactionHistory; }

    public String getTenantStatus() { return tenantStatus; }
    public void setTenantStatus(String tenantStatus) { this.tenantStatus = tenantStatus; }

    // **NEW METHOD: Calculate collection percentage**
    public double getCollectionPercentage() {
        if (tenant == null || tenant.rentAmount == 0) {
            return 0.0;
        }
        return (amountPaid / tenant.rentAmount) * 100.0;
    }

    // **OPTIONAL: Get formatted collection percentage as string**
    public String getCollectionPercentageFormatted() {
        return String.format("%.1f%%", getCollectionPercentage());
    }
}
