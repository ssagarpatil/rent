package com.ss.rentmangment;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantPagerAdapter extends FragmentStateAdapter {

    private TenantsFragment parentFragment;
    private Map<Integer, TenantListFragment> fragmentMap = new HashMap<>();
    private List<Tenant> cachedStudents = new ArrayList<>();
    private List<Tenant> cachedFamilies = new ArrayList<>();

    public TenantPagerAdapter(TenantsFragment fragment) {
        super(fragment);
        this.parentFragment = fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("TenantPagerAdapter", "createFragment called for position: " + position);

        TenantListFragment fragment;
        switch (position) {
            case 0: // Students tab
                fragment = TenantListFragment.newInstance("students");
                // Pass cached data if available
                if (!cachedStudents.isEmpty()) {
                    Log.d("TenantPagerAdapter", "Passing cached students to new fragment: " + cachedStudents.size());
                    fragment.updateTenants(cachedStudents);
                }
                break;
            case 1: // Families tab
                fragment = TenantListFragment.newInstance("families");
                // Pass cached data if available
                if (!cachedFamilies.isEmpty()) {
                    Log.d("TenantPagerAdapter", "Passing cached families to new fragment: " + cachedFamilies.size());
                    fragment.updateTenants(cachedFamilies);
                }
                break;
            default:
                fragment = TenantListFragment.newInstance("students");
                break;
        }

        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    /**
     * Update data in fragments and cache it
     */
    public void updateData(List<Tenant> allTenants) {
        if (allTenants == null) {
            allTenants = new ArrayList<>();
        }

        // Get filtered lists from parent fragment
        List<Tenant> students = parentFragment.getStudentTenants();
        List<Tenant> families = parentFragment.getFamilyTenants();

        // Cache the data
        cachedStudents = new ArrayList<>(students);
        cachedFamilies = new ArrayList<>(families);

        Log.d("TenantPagerAdapter", "updateData called:");
        Log.d("TenantPagerAdapter", "Total tenants: " + allTenants.size());
        Log.d("TenantPagerAdapter", "Students: " + students.size());
        Log.d("TenantPagerAdapter", "Families: " + families.size());

        // Update existing fragments
        TenantListFragment studentFragment = fragmentMap.get(0);
        TenantListFragment familyFragment = fragmentMap.get(1);

        if (studentFragment != null) {
            Log.d("TenantPagerAdapter", "Updating existing student fragment");
            studentFragment.updateTenants(students);
        }

        if (familyFragment != null) {
            Log.d("TenantPagerAdapter", "Updating existing family fragment");
            familyFragment.updateTenants(families);
        }
    }

    /**
     * Force update all fragments (call this in onResume)
     */
    public void forceUpdateAllFragments() {
        Log.d("TenantPagerAdapter", "forceUpdateAllFragments called");

        TenantListFragment studentFragment = fragmentMap.get(0);
        TenantListFragment familyFragment = fragmentMap.get(1);

        if (studentFragment != null && !cachedStudents.isEmpty()) {
            Log.d("TenantPagerAdapter", "Force updating student fragment with " + cachedStudents.size() + " students");
            studentFragment.updateTenants(cachedStudents);
        }

        if (familyFragment != null && !cachedFamilies.isEmpty()) {
            Log.d("TenantPagerAdapter", "Force updating family fragment with " + cachedFamilies.size() + " families");
            familyFragment.updateTenants(cachedFamilies);
        }
    }

    /**
     * Clear cached data when adapter is destroyed
     */
    public void clearCache() {
        cachedStudents.clear();
        cachedFamilies.clear();
        fragmentMap.clear();
    }
}
