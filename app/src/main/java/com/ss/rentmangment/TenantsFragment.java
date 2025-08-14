package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.content.Context.MODE_PRIVATE;

public class TenantsFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddTenant;
    private TextView tvTotalCount;

    private TenantPagerAdapter pagerAdapter;
    private DatabaseReference usersRef;
    private String adminMobile;

    private List<Tenant> allTenants = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tenants_tabbed, container, false);

        initViews(view);
        setupViewPager();

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get admin mobile from session
        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        adminMobile = sp.getString("mobile", "");

        fabAddTenant.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTenantActivity.class))
        );

        loadTenants();
        return view;
    }

    private void initViews(View view) {
        tvTotalCount = view.findViewById(R.id.tvTotalCount);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabAddTenant = view.findViewById(R.id.fabAddTenant);
    }

    private void setupViewPager() {
        pagerAdapter = new TenantPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
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
        usersRef.child(adminMobile).child("tenants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allTenants.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Tenant tenant = ds.getValue(Tenant.class);
                            if (tenant != null) {
                                allTenants.add(tenant);
                            }
                        }

                        updateUI();
                        pagerAdapter.updateData(allTenants);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load tenants", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        int studentCount = 0;
        int familyCount = 0;

        for (Tenant tenant : allTenants) {
            if (tenant.tenantType != null) {
                if (tenant.tenantType.equals("Family")) {
                    familyCount++;
                } else if (tenant.tenantType.equals("Students")) {
                    studentCount++;
                }
            }
        }

        String totalText = "Total: " + allTenants.size() + " tenants (" +
                studentCount + " students, " + familyCount + " families)";
        tvTotalCount.setText(totalText);

        // Update tab titles with counts
        TabLayout.Tab studentTab = tabLayout.getTabAt(0);
        TabLayout.Tab familyTab = tabLayout.getTabAt(1);

        if (studentTab != null) {
            studentTab.setText("🎓 Students (" + studentCount + ")");
        }
        if (familyTab != null) {
            familyTab.setText("🏠 Families (" + familyCount + ")");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTenants();
    }
}
