package com.ss.rentmangment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class TenantPagerAdapter extends FragmentStateAdapter {

    private List<Tenant> allTenants = new ArrayList<>();
    private TenantListFragment studentFragment;
    private TenantListFragment familyFragment;

    public TenantPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Students tab
                studentFragment = TenantListFragment.newInstance("students");
                return studentFragment;
            case 1:
                // Families tab
                familyFragment = TenantListFragment.newInstance("families");
                return familyFragment;
            default:
                return TenantListFragment.newInstance("students");
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Students and Families
    }

    public void updateData(List<Tenant> tenants) {
        this.allTenants = tenants;

        // Update fragments if they exist
        if (studentFragment != null) {
            studentFragment.updateTenants(filterStudents(tenants));
        }
        if (familyFragment != null) {
            familyFragment.updateTenants(filterFamilies(tenants));
        }
    }

    private List<Tenant> filterStudents(List<Tenant> tenants) {
        List<Tenant> students = new ArrayList<>();
        for (Tenant tenant : tenants) {
            if (!isFamily(tenant)) {
                students.add(tenant);
            }
        }
        return students;
    }

    private List<Tenant> filterFamilies(List<Tenant> tenants) {
        List<Tenant> families = new ArrayList<>();
        for (Tenant tenant : tenants) {
            if (isFamily(tenant)) {
                families.add(tenant);
            }
        }
        return families;
    }

    private boolean isFamily(Tenant tenant) {
        return (tenant.emergencyContactName != null && !tenant.emergencyContactName.isEmpty()) ||
                (tenant.securityDeposit >= tenant.rentAmount * 2);
    }
}
