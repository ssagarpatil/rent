//package com.ss.rentmangment;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.appcompat.widget.SearchView;  // ← CHANGED THIS LINE
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.viewpager2.widget.ViewPager2;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.tabs.TabLayout;
//import com.google.android.material.tabs.TabLayoutMediator;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.content.Context.MODE_PRIVATE;
//
//public class TenantsFragment extends Fragment {
//
//    private ViewPager2 viewPager;
//    private TabLayout tabLayout;
//    private FloatingActionButton fabAddTenant;
//    private TextView tvTotalCount;
//    private SearchView searchView;  // This will now be androidx.appcompat.widget.SearchView
//
//    private TenantPagerAdapter pagerAdapter;
//    private DatabaseReference usersRef;
//    private String adminMobile;
//    private ValueEventListener tenantsListener;
//
//    private List<Tenant> allTenants = new ArrayList<>();
//    private List<Tenant> filteredTenants = new ArrayList<>();
//    private boolean isDataLoaded = false;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_tenants_tabbed, container, false);
//
//        initViews(view);
//        setupViewPager();
//
//        usersRef = FirebaseDatabase.getInstance().getReference("users");
//
//        // Get admin mobile from session
//        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        adminMobile = sp.getString("mobile", "");
//
//        fabAddTenant.setOnClickListener(v ->
//                startActivity(new Intent(getContext(), AddTenantActivity.class))
//        );
//
//        setupSearchView();
//        loadTenants();
//
//        return view;
//    }
//
//    private void initViews(View view) {
//        tvTotalCount = view.findViewById(R.id.tvTotalCount);
//        tabLayout = view.findViewById(R.id.tabLayout);
//        viewPager = view.findViewById(R.id.viewPager);
//        fabAddTenant = view.findViewById(R.id.fabAddTenant);
//        searchView = view.findViewById(R.id.searchView);  // Now correctly casts to androidx SearchView
//    }
//
//    /** Determines if a tenant is a family based on tenantType field */
//    private boolean isTenantFamily(Tenant tenant) {
//        if (tenant == null) return false;
//
//        Log.d("FamilyCheck", "Checking tenant: " + tenant.name + ", tenantType: '" + tenant.tenantType + "'");
//
//        // Primary check: Firebase tenantType field
//        if (tenant.tenantType != null && !tenant.tenantType.trim().isEmpty()) {
//            String type = tenant.tenantType.trim();
//
//            if ("Family".equalsIgnoreCase(type)) {
//                Log.d("FamilyCheck", tenant.name + " is FAMILY (tenantType = " + type + ")");
//                return true;
//            } else if ("Students".equalsIgnoreCase(type) || "Student".equalsIgnoreCase(type)) {
//                Log.d("FamilyCheck", tenant.name + " is STUDENT (tenantType = " + type + ")");
//                return false;
//            }
//        }
//
//        // Fallback logic if tenantType is not set or has different values
//        boolean hasEmergencyContact = tenant.emergencyContactName != null &&
//                !tenant.emergencyContactName.trim().isEmpty();
//        boolean hasHighDeposit = tenant.securityDeposit > 0 && tenant.rentAmount > 0 &&
//                tenant.securityDeposit >= (tenant.rentAmount * 2);
//
//        boolean result = hasEmergencyContact || hasHighDeposit;
//        Log.d("FamilyCheck", tenant.name + " fallback logic result: " + result);
//
//        return result;
//    }
//
//    /** Get filtered list of students */
//    public List<Tenant> getStudentTenants() {
//        List<Tenant> students = new ArrayList<>();
//        for (Tenant tenant : filteredTenants) {
//            if (!isTenantFamily(tenant)) {
//                students.add(tenant);
//            }
//        }
//        Log.d("TenantsFragment", "Filtered students: " + students.size());
//        return students;
//    }
//
//    /** Get filtered list of families */
//    public List<Tenant> getFamilyTenants() {
//        List<Tenant> families = new ArrayList<>();
//        for (Tenant tenant : filteredTenants) {
//            if (isTenantFamily(tenant)) {
//                families.add(tenant);
//            }
//        }
//        Log.d("TenantsFragment", "Filtered families: " + families.size());
//        return families;
//    }
//
//    private void setupViewPager() {
//        pagerAdapter = new TenantPagerAdapter(this);
//        viewPager.setAdapter(pagerAdapter);
//
//        // Connect TabLayout with ViewPager2
//        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
//            switch (position) {
//                case 0:
//                    tab.setText("🎓 Students");
//                    break;
//                case 1:
//                    tab.setText("🏠 Families");
//                    break;
//            }
//        }).attach();
//    }
//
//    private void loadTenants() {
//        if (adminMobile == null || adminMobile.isEmpty()) {
//            Toast.makeText(getContext(), "Admin mobile not found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Remove existing listener if any
//        if (tenantsListener != null) {
//            usersRef.child(adminMobile).child("tenants").removeEventListener(tenantsListener);
//        }
//
//        tenantsListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d("TenantsFragment", "=== onDataChange called ===");
//                allTenants.clear();
//
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Tenant tenant = ds.getValue(Tenant.class);
//                    if (tenant != null) {
//                        allTenants.add(tenant);
//                        Log.d("TenantsFragment", "Loaded tenant: " + tenant.name +
//                                " with tenantType: " + tenant.tenantType);
//                    }
//                }
//                filteredTenants = new ArrayList<>(allTenants); // Initialize filtered list
//
//                Log.d("TenantsFragment", "Total tenants loaded: " + allTenants.size());
//                isDataLoaded = true;
//
//                updateUI();
//
//                // Update pager adapter with delay to ensure fragments are ready
//                viewPager.post(() -> {
//                    if (pagerAdapter != null) {
//                        pagerAdapter.updateData(filteredTenants);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load tenants: " + error.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//                Log.e("TenantsFragment", "Database error: " + error.getMessage());
//            }
//        };
//
//        usersRef.child(adminMobile).child("tenants").addValueEventListener(tenantsListener);
//    }
//
//    private void setupSearchView() {
//        if (searchView != null) {
//            searchView.setQueryHint("Search by name,mobile,room number");
//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    filterTenants(query);
//                    return false;
//                }
//
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    filterTenants(newText);
//                    return false;
//                }
//            });
//
//            // Clear search when X is clicked
//            searchView.setOnCloseListener(() -> {
//                filterTenants("");
//                return false;
//            });
//        }
//    }
//
//    private void filterTenants(String query) {
//        if (query == null || query.trim().isEmpty()) {
//            filteredTenants = new ArrayList<>(allTenants);
//        } else {
//            List<Tenant> filteredList = new ArrayList<>();
//            String lowerQuery = query.toLowerCase().trim();
//
//            for (Tenant tenant : allTenants) {
//                boolean matchesName = tenant.name != null &&
//                        tenant.name.toLowerCase().contains(lowerQuery);
//
//                boolean matchesMobile = tenant.mobile != null &&
//                        tenant.mobile.toLowerCase().contains(lowerQuery);
//
//                boolean matchesRoom = tenant.roomNumber != null &&
//                        tenant.roomNumber.toLowerCase().contains(lowerQuery);
//
//                if (matchesName || matchesMobile || matchesRoom) {
//                    filteredList.add(tenant);
//                }
//            }
//            filteredTenants = filteredList;
//        }
//
//        updateUI();
//
//        // Update adapter with filtered data
//        if (pagerAdapter != null) {
//            pagerAdapter.updateData(filteredTenants);
//            pagerAdapter.forceUpdateAllFragments();
//        }
//    }
//
//    private void updateUI() {
//        int studentCount = 0;
//        int familyCount = 0;
//
//        // Count using the same logic as filtering
//        for (Tenant tenant : filteredTenants) {
//            if (isTenantFamily(tenant)) {
//                familyCount++;
//            } else {
//                studentCount++;
//            }
//        }
//
//        String totalText = "Total: " + filteredTenants.size() + " tenants (" +
//                studentCount + " students, " + familyCount + " families)";
//        tvTotalCount.setText(totalText);
//
//        Log.d("TenantsFragment", "UI Updated - Students: " + studentCount + ", Families: " + familyCount);
//
//        // Update tab titles with counts
//        TabLayout.Tab studentTab = tabLayout.getTabAt(0);
//        TabLayout.Tab familyTab = tabLayout.getTabAt(1);
//
//        if (studentTab != null) {
//            studentTab.setText("🎓 Students (" + studentCount + ")");
//        }
//        if (familyTab != null) {
//            familyTab.setText("🏠 Families (" + familyCount + ")");
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("TenantsFragment", "=== onResume called ===");
//
//        // If data was already loaded, force update the fragments
//        if (isDataLoaded && pagerAdapter != null && !allTenants.isEmpty()) {
//            Log.d("TenantsFragment", "Data already loaded, forcing fragment updates");
//
//            // Small delay to ensure fragments are ready
//            viewPager.postDelayed(() -> {
//                pagerAdapter.updateData(filteredTenants);
//                pagerAdapter.forceUpdateAllFragments();
//            }, 100);
//        } else {
//            Log.d("TenantsFragment", "Loading tenants on resume");
//            loadTenants();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.d("TenantsFragment", "=== onPause called ===");
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Clean up listener
//        if (tenantsListener != null && usersRef != null && adminMobile != null) {
//            usersRef.child(adminMobile).child("tenants").removeEventListener(tenantsListener);
//        }
//
//        // Clear adapter cache
//        if (pagerAdapter != null) {
//            pagerAdapter.clearCache();
//        }
//    }
//}



package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;

public class TenantsFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddTenant;
    private TextView tvTotalCount;
    private SearchView searchView;

    private TenantPagerAdapter pagerAdapter;
    private DatabaseReference tenantsRef;
    private String adminMobile;
    private ValueEventListener tenantsListener;

    private List<Tenant> allTenants = new ArrayList<>();
    private List<Tenant> activeTenants = new ArrayList<>(); // List for active tenants only

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenants_tabbed, container, false);
        initViews(view);
        setupViewPager();

        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        adminMobile = sp.getString("mobile", "");
        tenantsRef = FirebaseDatabase.getInstance().getReference("users").child(adminMobile).child("tenants");

        fabAddTenant.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTenantActivity.class))
        );

        setupSearchView();
        return view;
    }

    private void initViews(View view) {
        tvTotalCount = view.findViewById(R.id.tvTotalCount);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabAddTenant = view.findViewById(R.id.fabAddTenant);
        searchView = view.findViewById(R.id.searchView);
    }

    private void setupViewPager() {
        pagerAdapter = new TenantPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("🎓 Students");
                    break;
                case 1:
                    tab.setText("🏠 Families");
                    break;
            }
        }).attach();
    }

    private void loadTenants() {
        if (adminMobile.isEmpty()) {
            Toast.makeText(getContext(), "Admin mobile not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tenantsListener != null) {
            tenantsRef.removeEventListener(tenantsListener);
        }

        tenantsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTenants.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Tenant tenant = ds.getValue(Tenant.class);
                    if (tenant != null) {
                        allTenants.add(tenant);
                    }
                }
                // Initial filter to show only active tenants
                filterTenants(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tenants.", Toast.LENGTH_SHORT).show();
            }
        };
        tenantsRef.orderByChild("name").addValueEventListener(tenantsListener);
    }

    private void setupSearchView() {
        searchView.setQueryHint("Search by name, mobile, or room");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTenants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTenants(newText);
                return true;
            }
        });
    }

    private void filterTenants(String query) {
        // Filter from the master list of all tenants
        List<Tenant> tempFiltered = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Tenant tenant : allTenants) {
            // IMPORTANT: Only include "Active" tenants in the list
            if (tenant.status == null || !tenant.status.equalsIgnoreCase("Active")) {
                continue;
            }

            if (lowerCaseQuery.isEmpty()) {
                tempFiltered.add(tenant);
            } else {
                boolean nameMatches = tenant.name != null && tenant.name.toLowerCase().contains(lowerCaseQuery);
                boolean mobileMatches = tenant.mobile != null && tenant.mobile.contains(lowerCaseQuery);
                boolean roomMatches = tenant.roomNumber != null && tenant.roomNumber.toLowerCase().contains(lowerCaseQuery);

                if (nameMatches || mobileMatches || roomMatches) {
                    tempFiltered.add(tenant);
                }
            }
        }

        activeTenants = tempFiltered;
        updateUIAndFragments();
    }

    private void updateUIAndFragments() {
        int studentCount = (int) activeTenants.stream().filter(t -> !isTenantFamily(t)).count();
        int familyCount = (int) activeTenants.stream().filter(this::isTenantFamily).count();

        tvTotalCount.setText("Active: " + activeTenants.size() + " (" + studentCount + " Students, " + familyCount + " Families)");

        // Update tab counts
        TabLayout.Tab studentTab = tabLayout.getTabAt(0);
        if (studentTab != null) studentTab.setText("🎓 Students (" + studentCount + ")");

        TabLayout.Tab familyTab = tabLayout.getTabAt(1);
        if (familyTab != null) familyTab.setText("🏠 Families (" + familyCount + ")");

        // Update adapter
        if (pagerAdapter != null) {
            pagerAdapter.updateData(getStudentTenants(), getFamilyTenants());
        }
    }

    // These methods provide the filtered lists to the PagerAdapter
    public List<Tenant> getStudentTenants() {
        return activeTenants.stream()
                .filter(t -> !isTenantFamily(t))
                .collect(Collectors.toList());
    }

    public List<Tenant> getFamilyTenants() {
        return activeTenants.stream()
                .filter(this::isTenantFamily)
                .collect(Collectors.toList());
    }

    // Correctly determines if a tenant is a family
    private boolean isTenantFamily(Tenant tenant) {
        return tenant != null && "Family".equalsIgnoreCase(tenant.tenantType);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always reload data on resume to catch changes from other activities
        loadTenants();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tenantsListener != null) {
            tenantsRef.removeEventListener(tenantsListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.setAdapter(null); // Clear adapter reference
    }
}
