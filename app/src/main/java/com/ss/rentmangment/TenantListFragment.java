//
//package com.ss.rentmangment;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TenantListFragment extends Fragment {
//
//    private static final String ARG_TYPE = "type";
//
//    private RecyclerView recyclerView;
//    private TextView tvEmptyMessage;
//    private TenantAdapter adapter;
//    private String fragmentType; // "students" or "families"
//    private List<Tenant> tenants = new ArrayList<>();
//    private boolean isViewCreated = false;
//
//    public static TenantListFragment newInstance(String type) {
//        TenantListFragment fragment = new TenantListFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_TYPE, type);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            fragmentType = getArguments().getString(ARG_TYPE, "students");
//        }
//        Log.d("TenantListFragment", "Fragment created for type: " + fragmentType);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        Log.d("TenantListFragment", "onCreateView called for " + fragmentType);
//
//        View view = inflater.inflate(R.layout.fragment_tenant_list, container, false);
//
//        recyclerView = view.findViewById(R.id.recyclerViewTenants);
//        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
//
//        setupRecyclerView();
//        isViewCreated = true;
//
//        // If we already have data, update immediately
//        if (!tenants.isEmpty()) {
//            Log.d("TenantListFragment", "onCreateView: Found existing data, updating immediately");
//            updateTenants(tenants);
//        } else {
//            updateEmptyState();
//        }
//
//        Log.d("TenantListFragment", "onCreateView completed for " + fragmentType);
//        return view;
//    }
//
//    private void setupRecyclerView() {
//        if (recyclerView != null) {
//            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//            adapter = new TenantAdapter(getContext(), tenants);
//            recyclerView.setAdapter(adapter);
//
//            Log.d("TenantListFragment", "RecyclerView setup for " + fragmentType +
//                    " with " + tenants.size() + " initial tenants");
//        }
//    }
//
//    /**
//     * Update tenants list from parent - THIS IS THE KEY METHOD
//     */
//    public void updateTenants(List<Tenant> newTenants) {
//        Log.d("TenantListFragment", "=== updateTenants called for " + fragmentType + " ===");
//        Log.d("TenantListFragment", "Received " + (newTenants != null ? newTenants.size() : 0) + " tenants");
//        Log.d("TenantListFragment", "isViewCreated: " + isViewCreated);
//
//        this.tenants = newTenants != null ? new ArrayList<>(newTenants) : new ArrayList<>();
//
//        // Debug: Log each tenant received
//        for (int i = 0; i < this.tenants.size(); i++) {
//            Tenant tenant = this.tenants.get(i);
//            Log.d("TenantListFragment", fragmentType + " - Tenant " + (i+1) + ": " +
//                    tenant.name + " (Type: " + tenant.tenantType + ")");
//        }
//
//        // Only update adapter if view is created
//        if (isViewCreated && adapter != null) {
//            Log.d("TenantListFragment", "Updating adapter for " + fragmentType);
//            adapter.updateList(this.tenants);
//        } else {
//            Log.w("TenantListFragment", "View not ready for " + fragmentType +
//                    " - isViewCreated: " + isViewCreated + ", adapter: " + (adapter != null));
//        }
//
//        updateEmptyState();
//        Log.d("TenantListFragment", "=== updateTenants completed for " + fragmentType + " ===");
//    }
//
//    private void updateEmptyState() {
//        if (!isViewCreated) return;
//
//        boolean isEmpty = tenants.isEmpty();
//
//        Log.d("TenantListFragment", fragmentType + " - isEmpty: " + isEmpty +
//                ", tenants.size(): " + tenants.size());
//
//        if (isEmpty) {
//            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
//            if (tvEmptyMessage != null) {
//                tvEmptyMessage.setVisibility(View.VISIBLE);
//                String emptyMessage = fragmentType.equals("families") ?
//                        "No family tenants found.\nAdd tenants with 'Family' type to see them here." :
//                        "No student tenants found.\nAdd tenants with 'Students' type to see them here.";
//                tvEmptyMessage.setText(emptyMessage);
//            }
//            Log.d("TenantListFragment", "Showing empty message for " + fragmentType);
//        } else {
//            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
//            if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(View.GONE);
//
//            Log.d("TenantListFragment", "Showing recycler view for " + fragmentType +
//                    " with " + tenants.size() + " items");
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("TenantListFragment", fragmentType + " fragment resumed with " +
//                tenants.size() + " tenants");
//
//        // Force refresh if we have data
//        if (!tenants.isEmpty() && adapter != null) {
//            Log.d("TenantListFragment", "onResume: Force refreshing adapter for " + fragmentType);
//            adapter.updateList(tenants);
//        }
//
//        updateEmptyState();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        isViewCreated = false;
//        Log.d("TenantListFragment", "onDestroyView called for " + fragmentType);
//    }
//}



package com.ss.rentmangment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TenantListFragment extends Fragment {

    private static final String ARG_TYPE = "type";

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private TenantAdapter adapter;
    private String fragmentType; // "students" or "families"
    private List<Tenant> tenants = new ArrayList<>();
    private boolean isViewCreated = false;

    public static TenantListFragment newInstance(String type) {
        TenantListFragment fragment = new TenantListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentType = getArguments().getString(ARG_TYPE, "students");
        }
        Log.d("TenantListFragment", "Fragment created for type: " + fragmentType);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("TenantListFragment", "onCreateView called for " + fragmentType);

        View view = inflater.inflate(R.layout.fragment_tenant_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTenants);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        setupRecyclerView();
        isViewCreated = true;

        // If we already have data, update immediately
        if (!tenants.isEmpty()) {
            Log.d("TenantListFragment", "onCreateView: Found existing data, updating immediately");
            updateTenants(tenants);
        } else {
            updateEmptyState();
        }

        Log.d("TenantListFragment", "onCreateView completed for " + fragmentType);
        return view;
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new TenantAdapter(getContext(), tenants);

            // ONLY enable room grouping for students - families remain unchanged
            if ("students".equals(fragmentType)) {
                adapter.setStudentMode(true);
                Log.d("TenantListFragment", "Room grouping ENABLED for students");
            } else {
                adapter.setStudentMode(false);
                Log.d("TenantListFragment", "Room grouping DISABLED for families (unchanged)");
            }

            recyclerView.setAdapter(adapter);

            Log.d("TenantListFragment", "RecyclerView setup for " + fragmentType +
                    " with " + tenants.size() + " initial tenants");
        }
    }

    /**
     * Update tenants list from parent
     */
    public void updateTenants(List<Tenant> newTenants) {
        Log.d("TenantListFragment", "=== updateTenants called for " + fragmentType + " ===");
        Log.d("TenantListFragment", "Received " + (newTenants != null ? newTenants.size() : 0) + " tenants");

        this.tenants = newTenants != null ? new ArrayList<>(newTenants) : new ArrayList<>();

        // Only update adapter if view is created
        if (isViewCreated && adapter != null) {
            Log.d("TenantListFragment", "Updating adapter for " + fragmentType);

            // Set mode before updating data
            if ("students".equals(fragmentType)) {
                adapter.setStudentMode(true); // Room grouping for students
            } else {
                adapter.setStudentMode(false); // No grouping for families
            }

            adapter.updateList(this.tenants);
        }

        updateEmptyState();
        Log.d("TenantListFragment", "=== updateTenants completed for " + fragmentType + " ===");
    }

    private void updateEmptyState() {
        if (!isViewCreated) return;

        boolean isEmpty = tenants.isEmpty();

        if (isEmpty) {
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setVisibility(View.VISIBLE);
                String emptyMessage = fragmentType.equals("families") ?
                        "No family tenants found.\nAdd tenants with 'Family' type to see them here." :
                        "No student tenants found.\nAdd tenants with 'Students' type to see them here.";
                tvEmptyMessage.setText(emptyMessage);
            }
        } else {
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Force refresh if we have data
        if (!tenants.isEmpty() && adapter != null) {
            // Ensure correct mode is set
            if ("students".equals(fragmentType)) {
                adapter.setStudentMode(true);
            } else {
                adapter.setStudentMode(false);
            }

            adapter.updateList(tenants);
        }

        updateEmptyState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        Log.d("TenantListFragment", "onDestroyView called for " + fragmentType);
    }
}
