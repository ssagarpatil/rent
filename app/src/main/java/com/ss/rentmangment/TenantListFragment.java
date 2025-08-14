package com.ss.rentmangment;

import android.os.Bundle;
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

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private TenantAdapter adapter;
    private List<Tenant> tenantList = new ArrayList<>();
    private String tenantType;

    public static TenantListFragment newInstance(String type) {
        TenantListFragment fragment = new TenantListFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tenantType = getArguments().getString("type", "students");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        setupRecyclerView();
        updateEmptyMessage();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TenantAdapter(getContext(), tenantList);
        recyclerView.setAdapter(adapter);
    }

    public void updateTenants(List<Tenant> tenants) {
        tenantList.clear();
        tenantList.addAll(tenants);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        if (tenantList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);

            if (tenantType.equals("students")) {
                tvEmptyMessage.setText("📚 No students yet\n\nAdd your first student tenant!");
            } else {
                tvEmptyMessage.setText("🏠 No families yet\n\nAdd your first family tenant!");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }
}
