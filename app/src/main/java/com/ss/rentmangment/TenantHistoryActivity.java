package com.ss.rentmangment;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TenantHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TenantHistoryAdapter adapter;
    private TabLayout tabLayout;
    private SearchView searchView;

    private List<Tenant> masterTenantList = new ArrayList<>(); // Stores ALL tenants
    private List<Tenant> displayedTenantList = new ArrayList<>(); // Stores tenants to be shown

    private DatabaseReference tenantsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_tenant_history);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewTenantHistory);
        tabLayout = findViewById(R.id.tabLayoutHistory);
        searchView = findViewById(R.id.searchViewHistory);

        adapter = new TenantHistoryAdapter(this, displayedTenantList);
        recyclerView.setAdapter(adapter);

        String adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("mobile", "");
        tenantsRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("tenants");

        setupTabs();
        setupSearchView();
        loadAllTenants();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Active Tenants"));
        tabLayout.addTab(tabLayout.newTab().setText("Archived / Left"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList();
                return true;
            }
        });
    }

    private void loadAllTenants() {
        tenantsRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                masterTenantList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Tenant tenant = ds.getValue(Tenant.class);
                    if (tenant != null) {
                        masterTenantList.add(tenant);
                    }
                }
                // Initial filter after loading data
                filterList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TenantHistoryActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This single method now handles ALL filtering logic (tabs and search).
     */
    private void filterList() {
        displayedTenantList.clear();
        String currentTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString();
        String searchQuery = searchView.getQuery().toString().toLowerCase().trim();

        for (Tenant tenant : masterTenantList) {
            // Step 1: Filter by Tab (Active vs. Left)
            boolean matchesTab = false;
            if (currentTab.contains("Active") && "Active".equalsIgnoreCase(tenant.status)) {
                matchesTab = true;
            } else if (currentTab.contains("Archived") && !"Active".equalsIgnoreCase(tenant.status)) {
                matchesTab = true;
            }

            if (matchesTab) {
                // Step 2: Filter by Search Query
                if (searchQuery.isEmpty()) {
                    displayedTenantList.add(tenant);
                } else {
                    boolean nameMatches = tenant.name != null && tenant.name.toLowerCase().contains(searchQuery);
                    boolean mobileMatches = tenant.mobile != null && tenant.mobile.contains(searchQuery);
                    boolean roomMatches = tenant.assignedRoomName != null && tenant.assignedRoomName.toLowerCase().contains(searchQuery);
                    if (nameMatches || mobileMatches || roomMatches) {
                        displayedTenantList.add(tenant);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
