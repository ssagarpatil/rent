package com.ss.rentmangment;



public class TenantProfile {
    private Tenant tenant;
    private boolean isPaidForCurrentMonth;
    private int pendingMonths;

    public TenantProfile(Tenant tenant) {
        this.tenant = tenant;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public boolean isPaidForCurrentMonth() {
        return isPaidForCurrentMonth;
    }

    public void setPaidForCurrentMonth(boolean paidForCurrentMonth) {
        isPaidForCurrentMonth = paidForCurrentMonth;
    }

    public int getPendingMonths() {
        return pendingMonths;
    }

    public void setPendingMonths(int pendingMonths) {
        this.pendingMonths = pendingMonths;
    }
}

